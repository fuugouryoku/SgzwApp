package nc.impl.oa.oaod.receiptregdoc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.ml.NCLangResOnserver;
import nc.bs.oa.oaco.adl.common.PersonUtil;
import nc.bs.oa.oaco.basecomp.associateform.AssociateFormUtil;
import nc.bs.oa.oaco.pub.base.OaSuperVOService;
import nc.bs.oa.oaod.archive.OAODArchiveHelper;
import nc.itf.oa.oaco.oadefdoc.IOADefDocService;
import nc.itf.oa.oaod.odorg.ICheckDocIsOver;
import nc.itf.oa.oaod.odswap.IODSwapReceiptDocPubService;
import nc.itf.oa.oaod.receiptdealdoc.IReceiptDealDocService;
import nc.itf.oa.oaod.receiptregdoc.IReceiptRegDocService;
import nc.itf.oa.oaod.swapitf.ISwapReceiptDocPub;
import nc.itf.oa.oapub.archivemanagement.IArchiveService;
import nc.jdbc.framework.generator.SequenceGenerator;
import nc.jdbc.framework.processor.MapProcessor;
import nc.md.persist.framework.IMDPersistenceQueryService;
import nc.md.persist.framework.MDPersistenceService;
import nc.uap.cpb.baseservice.IUifCpbService;
import nc.uap.cpb.persist.dao.PtBaseDAO;
import nc.uap.lfw.core.LfwRuntimeEnvironment;
import nc.uap.lfw.core.crud.CRUDHelper;
import nc.uap.lfw.core.data.PaginationInfo;
import nc.uap.lfw.core.exception.LfwBusinessException;
import nc.uap.lfw.core.exception.LfwRuntimeException;
import nc.uap.lfw.core.log.LfwLogger;
import nc.uap.lfw.file.FileManager;
import nc.uap.lfw.file.vo.LfwFileVO;
import nc.uap.wfm.itf.IWfmProInsBill;
import nc.uap.wfm.itf.IWfmProInsQry;
import nc.uap.wfm.vo.WfmProInsVO;
import nc.util.oa.oaod.OAODUtil;
import nc.vo.bd.psn.PsndocVO;
import nc.vo.bd.psn.PsnjobVO;
import nc.vo.oa.oaod.commonenum.BillStatusEnum;
import nc.vo.oa.oaod.receiptdealdoc.DealTypeEnum;
import nc.vo.oa.oaod.receiptdealdoc.ReceiptDealDocVO;
import nc.vo.oa.oaod.receiptregdoc.DocOriginEnum;
import nc.vo.oa.oaod.receiptregdoc.ReceiptRegDocVO;
import nc.vo.org.AdminOrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import org.apache.commons.lang.StringUtils;

public class ReceiptRegDocServiceImpl extends OaSuperVOService<ReceiptRegDocVO>
  implements IReceiptRegDocService, ICheckDocIsOver
{
  PtBaseDAO dao = new PtBaseDAO();

  public ReceiptRegDocServiceImpl() {
    super("4359b387-e6bc-4199-b52a-a47dcbb6ad99", ReceiptRegDocVO.class, null);
  }

  @Override
public ReceiptRegDocVO save(ReceiptRegDocVO vo)
    throws BusinessException
  {
    AssociateFormUtil.saveRelativeForm(vo.getPk_receiptregdoc());
    return (super.doSave(vo));
  }

  @Override
protected void fireAfterInsertEvent(ReceiptRegDocVO vo)
    throws BusinessException
  {
    if ((vo.getDocorigin().equals(DocOriginEnum.SWAP.value().toString())) && 
      (!(vo.getBillstatus().equalsIgnoreCase(BillStatusEnum.CANCELLATION.value().toString()))))
    {
      IODSwapReceiptDocPubService service = NCLocator.getInstance().lookup(IODSwapReceiptDocPubService.class);

      service.setSwapReceiptDocStateToSigned(vo.getPk_swapdoc(), vo.getRecipient());
    }

    FileManager fileManager = OAODUtil.getSystemFileManagerByBafile();
    try {
      LfwFileVO lfwFileVO = fileManager.getFileVO(vo.getPk_file());
      lfwFileVO.setPk_billitem(vo.getPk_receiptregdoc());

      fileManager.updateVo(lfwFileVO);
      
      //附件修改~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
      LfwFileVO[] files = fileManager.getFileByItemID(vo.getFileitem());
      if ((files != null) && (files.length > 0)) {
        for (LfwFileVO file : files) {
          file.setPk_billitem(vo.getPk_receiptregdoc()+"_file");
          file.setPk_billtype("wfm_attach");
          file.setStatus(1);
          fileManager.updateVo(file);
        }
      }
      vo.setFileitem(vo.getPk_receiptregdoc()+"_file");
    } catch (LfwBusinessException e) {
      throw new LfwRuntimeException(e);
    }
    
    super.fireAfterInsertEvent(vo);
  }

  @Override
protected void fireBeforeDeleteEvent(ReceiptRegDocVO vo)
    throws BusinessException
  {
    if (!(vo.getBillstatus().equals(BillStatusEnum.NOTTSTART.value().toString())))
    {
      throw new LfwRuntimeException(NCLangResOnserver.getInstance().getStrByID("odreceiptdocmgr", "ReceiptRegDocServiceImpl-000000"));
    }

    if (isOverOrArchive(vo)) {
      throw new LfwRuntimeException(NCLangResOnserver.getInstance().getStrByID("odreceiptdocmgr", "ReceiptRegDocServiceImpl-000001"));
    }

    IReceiptDealDocService dealservice = NCLocator.getInstance().lookup(IReceiptDealDocService.class);
    try
    {
      if (dealservice.existDealDocByRegDoc(vo.getPk_receiptregdoc())) {
        throw new BusinessException(NCLangResOnserver.getInstance().getStrByID("odreceiptdocmgr", "ReceiptRegDocServiceImpl-000002"));
      }
    }
    catch (LfwBusinessException e)
    {
      throw new BusinessException(e.getMessage());
    }
    super.fireBeforeDeleteEvent(vo);
  }

  @Override
protected void fireAfterDeleteEvent(ReceiptRegDocVO vo)
    throws BusinessException
  {
    if ((vo.getDocorigin().equals(DocOriginEnum.SWAP.value().toString())) && 
      (!(vo.getBillstatus().equalsIgnoreCase(BillStatusEnum.CANCELLATION.value().toString()))))
    {
      IODSwapReceiptDocPubService service = NCLocator.getInstance().lookup(IODSwapReceiptDocPubService.class);

      service.setSwapReceiptDocStateToPresign(vo.getPk_swapdoc());
    }

    try
    {
      int i;
      FileManager filemgr = OAODUtil.getSystemFileManagerByBafile();
      LfwFileVO[] lfwFileVOs = filemgr.getFileByItemID(vo.getPrimaryKey());

      if (lfwFileVOs != null) {
        for (i = 0; i < lfwFileVOs.length; ++i)
          filemgr.delete(lfwFileVOs[i].getPk_lfwfile());
      }
      lfwFileVOs = filemgr.getFileByItemID(vo.getPrimaryKey() + "_file");
      if (lfwFileVOs != null)
        for (i = 0; i < lfwFileVOs.length; ++i)
          filemgr.delete(lfwFileVOs[i].getPk_lfwfile());
    }
    catch (Exception e) {
      throw new LfwRuntimeException(e.getMessage());
    }
    IWfmProInsQry wfmInsQry = NCLocator.getInstance().lookup(IWfmProInsQry.class);

    IWfmProInsBill wfmInsBill = NCLocator.getInstance().lookup(IWfmProInsBill.class);

    WfmProInsVO[] vos = wfmInsQry.getAllProInsByFormInsPk(vo.getPk_receiptregdoc());

    if (vos != null) {
      for (int i = 0; i < vos.length; ++i) {
        String pk_proin = vos[i].getPk_proins();
        wfmInsBill.deleteProInsByProInsPk(pk_proin);
      }
    }
    super.fireAfterDeleteEvent(vo);
  }

  @Override
protected void fireAfterUpdateEvent(ReceiptRegDocVO oldvo, ReceiptRegDocVO newvo)
    throws BusinessException
  {
    if (((!(oldvo.getBillstatus().equals(BillStatusEnum.NOTTSTART.value()))) && (!(oldvo.getBillstatus().equals(BillStatusEnum.RUN.value())))) || (!(newvo.getBillstatus().equals(BillStatusEnum.END.value()))) || 
      (!(OAODArchiveHelper.isAutoArchive(newvo.getDoctype())))) return;
    try {
      if ((newvo.getIsover() != null) && (!(newvo.getIsover().booleanValue())))
      {
        newvo = overDoc(newvo); }
      archiveDoc(newvo);
    } catch (Exception e) {
      LfwLogger.error("auto archive if failure:" + newvo.getPk_receiptregdoc());

      LfwLogger.error(e);
    }
  }

  @Override
public ReceiptRegDocVO[] getReceiptRegDocVOs(String whereSql, String orderSql, PaginationInfo paginationInfo)
    throws BusinessException
  {
    ReceiptRegDocVO[] vos = null;
    try
    {
      vos = getOaQueryService().queryVOs(new ReceiptRegDocVO(), paginationInfo, whereSql, null, orderSql);
    }
    catch (Exception e)
    {
      throw new LfwRuntimeException(e);
    }
    return vos;
  }

  @Override
public ReceiptRegDocVO[] getReceiptRegDocVOs(String whereSql, PaginationInfo paginationInfo)
    throws BusinessException
  {
    return getReceiptRegDocVOs(whereSql, "", paginationInfo);
  }

  @Override
public ReceiptRegDocVO getNewReceiptRegDocVO(String pk_org, String doctype, String swapdoc)
    throws BusinessException
  {
    ReceiptRegDocVO newvo = newReceiptRegDocVO(pk_org, doctype);
    newvo.setPk_swapdoc(swapdoc);
    newvo.setDocorigin(DocOriginEnum.SWAP.value().toString());

    IODSwapReceiptDocPubService service = NCLocator.getInstance().lookup(IODSwapReceiptDocPubService.class);

    ISwapReceiptDocPub vo = service.getSwapReceiptDoc(swapdoc);

    newvo.setDispatchunit(vo.getDispatchunit());
    newvo.setTitle(vo.getTitle());
    newvo.setDispatchno(vo.getDispatchno());
    newvo.setSubject(vo.getSubject());
    newvo.setIssuer(vo.getIssuer());
    newvo.setIssuedate(vo.getIssuedate());
    newvo.setDef1(vo.getDef1());
    newvo.setDef2(vo.getDef2());
    newvo.setDef3(vo.getDef3());
    newvo.setDef4(vo.getDef4());
    newvo.setDef5(vo.getDef5());
    newvo.setDef6(vo.getDef6());
    newvo.setDef7(vo.getDef7());
    newvo.setDef8(vo.getDef8());
    newvo.setDef9(vo.getDef9());
    newvo.setDef10(vo.getDef10());
    newvo.setDef11(vo.getDef11());
    newvo.setDef12(vo.getDef12());
    newvo.setDef13(vo.getDef13());
    newvo.setDef14(vo.getDef14());
    newvo.setDef15(vo.getDef15());
    newvo.setDef16(vo.getDef16());
    newvo.setDef17(vo.getDef17());
    newvo.setDef18(vo.getDef18());
    newvo.setDef19(vo.getDef19());
    newvo.setDef20(vo.getDef20());

    newvo.setOpinion1(vo.getOpinion1());
    newvo.setOpinion2(vo.getOpinion2());
    newvo.setOpinion3(vo.getOpinion3());
    newvo.setOpinion4(vo.getOpinion4());
    newvo.setOpinion5(vo.getOpinion5());

    String currentUser = LfwRuntimeEnvironment.getLfwSessionBean().getUser_name();
    newvo.setRecipient(currentUser);

    IOADefDocService srv = NCLocator.getInstance().lookup(IOADefDocService.class);

    AdminOrgVO receiptOrg = NCLocator.getInstance().lookup(nc.itf.oa.oaod.odorg.IODOrgMgrScopeService.class).GetTheAdminOrgVO(String.format(" %s='%s' ", new Object[] { "pk_adminorg", pk_org }))[0];

    String fromOrgName = vo.getDispatchunit();
    AdminOrgVO fromOrg = NCLocator.getInstance().lookup(nc.itf.oa.oaod.odorg.IODOrgMgrScopeService.class).GetTheAdminOrgVO(String.format(" %s='%s' ", new Object[] { nc.bs.oa.oaco.pub.util.MultiLangUtil.getCurrentMultiFieldName("name"), fromOrgName.toString().split(" ")[0] }))[0];

    if (!(receiptOrg.getPk_group().equals(fromOrg.getPk_group())))
    {
      String group = String.format(" %s='%s' ", new Object[] { "pk_group", receiptOrg.getPk_group() });

      newvo.setDirection(OAODUtil.getReceiveGroupDefPk(srv, group, vo.getDirection()));
      newvo.setUrgentdegree(OAODUtil.getReceiveGroupDefPk(srv, group, vo.getUrgentdegree()));
      newvo.setSecuritylevel(OAODUtil.getReceiveGroupDefPk(srv, group, vo.getSecuritylevel()));
      newvo.setExpriedate(OAODUtil.getReceiveGroupDefPk(srv, group, vo.getExpriedate()));
      newvo.setCategory(OAODUtil.getReceiveGroupDefPk(srv, group, vo.getFiletype()));
    }
    else {
      newvo.setDirection(vo.getDirection());
      newvo.setUrgentdegree(vo.getUrgentdegree());
      newvo.setSecuritylevel(vo.getSecuritylevel());
      newvo.setExpriedate(vo.getExpriedate());
      newvo.setCategory(vo.getFiletype());
    }
    try {
      FileManager filemgr = OAODUtil.getSystemFileManagerByBafile();
      LfwFileVO fileVO = filemgr.getFileVO(vo.getPk_file());
      if (null != fileVO) {
        newvo.setPk_file(filemgr.copyFile(fileVO.getFilename(), "wfm_attach", null, null, vo.getPk_file()));
      }

      LfwFileVO[] files = filemgr.getAttachFileByItemID(swapdoc);
      if (files != null) {
        for (LfwFileVO file : files) {
          filemgr.copyFile(file.getFilename(), "wfm_attach", newvo.getFileitem(), null, file.getPk_lfwfile());
        }
      }
    }
    catch (Exception e)
    {
      throw new LfwRuntimeException(e);
    }
    return newvo;
  }

  @Override
public ReceiptRegDocVO getNewReceiptRegDocVO(String pk_org, String doctype)
  {
    ReceiptRegDocVO newvo = newReceiptRegDocVO(pk_org, doctype);
    newvo.setDocorigin(DocOriginEnum.NORMAL.value().toString());
    try
    {
      FileManager fileMgr = OAODUtil.getSystemFileManagerByBafile();
      InputStream ins = null;
      try {
        ins = fileMgr.getEmptyWordStream();
        String wordPk = fileMgr.upload("reg.docx", null, "", 0L, ins);
        newvo.setPk_file(wordPk);
        try
        {
          ins.close();
        } catch (IOException e) {
          throw new LfwRuntimeException(e);
        }
      }
      catch (Exception e)
      {
      }
      finally
      {
        if (ins != null) {
          try {
            ins.close();
          } catch (IOException e) {
            throw new LfwRuntimeException(e);
          }
        }
      }
    }
    catch (Exception e)
    {
      throw new LfwRuntimeException(e.getMessage());
    }
    return newvo;
  }

  private ReceiptRegDocVO newReceiptRegDocVO(String pk_org, String doctype) {
    ReceiptRegDocVO newvo = new ReceiptRegDocVO();
    newvo.setPk_group(InvocationInfoProxy.getInstance().getGroupId());
    newvo.setPk_org(pk_org);
    newvo.setDoctype(doctype);
    newvo.setDealtype(DealTypeEnum.NOTHING.value().toString());

    newvo.setDealer(LfwRuntimeEnvironment.getLfwSessionBean().getPk_user());

    newvo.setRegistrant(InvocationInfoProxy.getInstance().getUserId());

    newvo.setReceiptdate(new UFDate());
    newvo.setIsarchive(UFBoolean.FALSE);
    newvo.setIsover(UFBoolean.FALSE);

    newvo.setPk_receiptregdoc(new SequenceGenerator().generate());
    newvo.setFileitem(newvo.getPrimaryKey() + "_file");
    newvo.setStatus(2);
    newvo.setBillstatus(BillStatusEnum.NOTTSTART.value().toString());
    return newvo;
  }

  @Override
public ReceiptRegDocVO getVOByPk(String pk)
    throws BusinessException
  {
    Collection vos = getOaQueryService().queryBillOfVOByCond(ReceiptRegDocVO.class, String.format(" %S = '%S' ", new Object[] { "pk_receiptregdoc", pk }), false);

    if ((vos != null) && (vos.size() > 0)) {
      ReceiptRegDocVO vo = ((ReceiptRegDocVO[])vos.toArray(new ReceiptRegDocVO[vos.size()]))[0];
      vo.setFileitem(vo.getPrimaryKey() + "_file");
      return vo;
    }
    return null;
  }

  private IMDPersistenceQueryService getMDQueryService()
  {
    return MDPersistenceService.lookupPersistenceQueryService();
  }

  @Override
public boolean isExistsRunningDealDoc(String pk)
  {
    boolean flag = false;

    String sql = "select count(*) as count from  oaod_receiptdealdoc where pk_receiptregdoc='%s'";
    sql = String.format(sql, new Object[] { pk });
    try
    {
      Map val = (Map)CRUDHelper.getCRUDService().query(sql, new MapProcessor());

      int ret = ((Integer)val.get("count")).intValue();
      if (ret > 0)
        flag = true;
      else
        flag = false;
    }
    catch (LfwBusinessException e)
    {
      e.printStackTrace();
    }
    return flag;
  }

  @Override
public void deleteDoc(ReceiptRegDocVO vo)
  {
    vo.setStatus(3);
    try {
      save(vo);
    }
    catch (BusinessException e) {
      throw new LfwRuntimeException(e.getMessage());
    }
  }

  @Override
public ReceiptRegDocVO cancelDoc(ReceiptRegDocVO vo)
    throws BusinessException
  {
    ReceiptDealDocVO[] dealvos;
    if (isOverOrArchive(vo)) {
      throw new LfwBusinessException(NCLangResOnserver.getInstance().getStrByID("odreceiptdocmgr", "ReceiptRegDocServiceImpl-000004"));
    }

    if (vo.getBillstatus().equalsIgnoreCase(BillStatusEnum.END.value().toString()))
    {
      throw new LfwBusinessException(NCLangResOnserver.getInstance().getStrByID("odreceiptdocmgr", "ReceiptRegDocServiceImpl-000003"));
    }

    IReceiptDealDocService dealservice = NCLocator.getInstance().lookup(IReceiptDealDocService.class);
    try
    {
      dealvos = dealservice.queryByWhere(String.format(" %s <> '%s' and %s = '%s' ", new Object[] { "billstatus", BillStatusEnum.CANCELLATION.value().toString(), "pk_receiptregdoc", vo.getPk_receiptregdoc() }), null);
    }
    catch (BusinessException e)
    {
      throw new LfwBusinessException(e.getMessage());
    }
    if ((dealvos != null) && (dealvos.length > 0)) {
      throw new LfwBusinessException(NCLangResOnserver.getInstance().getStrByID("odreceiptdocmgr", "ReceiptRegDocServiceImpl-000005"));
    }

    vo.setStatus(1);
    vo.setBillstatus(BillStatusEnum.CANCELLATION.value().toString());
    if (vo.getDocorigin().equals(DocOriginEnum.SWAP.value().toString())) {
      IODSwapReceiptDocPubService service = NCLocator.getInstance().lookup(IODSwapReceiptDocPubService.class);

      service.setSwapReceiptDocStateToPresign(vo.getPk_swapdoc());
      vo.setDocorigin(DocOriginEnum.NORMAL.value().toString());
      vo.setPk_swapdoc(null);
    }
    try {
      return save(vo);
    }
    catch (BusinessException service) {
      throw new LfwBusinessException(service.getMessage());
    }
  }

  @Override
public void CheckDocIsOver(String orgid)
    throws BusinessException
  {
    ReceiptRegDocVO[] vos = getReceiptRegDocVOs(String.format(" pk_org = '%s' and docstate in('Run','NottStart','Suspended') ", new Object[] { orgid }), null);

    if ((vos != null) && (vos.length > 0))
      throw new LfwRuntimeException(NCLangResOnserver.getInstance().getStrByID("odreceiptdocmgr", "ReceiptRegDocServiceImpl-000006"));
  }

  @Override
public ReceiptRegDocVO overDoc(ReceiptRegDocVO vo)
    throws LfwBusinessException
  {
    canOverDoc(vo);
    vo.setIsover(UFBoolean.TRUE);
    if (vo.getStatus() == 0)
      vo.setStatus(1);
    try {
      return save(vo);
    }
    catch (BusinessException e) {
      throw new LfwBusinessException(e.getMessage());
    }
  }

  @Override
public void canOverDoc(ReceiptRegDocVO vo) throws LfwBusinessException {
    ReceiptDealDocVO[] dealvos;
    try {
      ReceiptRegDocVO[] vos = getReceiptRegDocVOsBySql(String.format(" where pk_receiptregdoc = '%s' ", new Object[] { vo.getPk_receiptregdoc() }));
      if ((vos != null) && (vos.length > 0))
        vo = vos[0];
    } catch (BusinessException e1) {
      throw new LfwRuntimeException(e1.getMessage());
    }
    if (vo.getIsover().booleanValue()) {
      throw new LfwBusinessException(NCLangResOnserver.getInstance().getStrByID("odreceiptdocmgr", "ReceiptRegDocServiceImpl-000007"));
    }

    IReceiptDealDocService dealservice = NCLocator.getInstance().lookup(IReceiptDealDocService.class);
    try
    {
      dealvos = dealservice.queryByWhere(String.format(" %s <> '%s' and %s <> '%s' and %s = '%s' ", new Object[] { "billstatus", BillStatusEnum.CANCELLATION.value().toString(), "billstatus", BillStatusEnum.END.value().toString(), "pk_receiptregdoc", vo.getPk_receiptregdoc() }), null);
    }
    catch (BusinessException e)
    {
      throw new LfwBusinessException(e.getMessage());
    }
    if ((dealvos != null) && (dealvos.length > 0))
      throw new LfwBusinessException(NCLangResOnserver.getInstance().getStrByID("odreceiptdocmgr", "ReceiptRegDocServiceImpl-000008"));
  }

  @Override
public ReceiptRegDocVO undoOverDoc(ReceiptRegDocVO vo)
    throws LfwBusinessException
  {
    canUndoOverDoc(vo);
    vo.setIsover(UFBoolean.FALSE);
    if (vo.getStatus() == 0)
      vo.setStatus(1);
    try {
      return save(vo);
    }
    catch (BusinessException e) {
      throw new LfwBusinessException(e.getMessage());
    }
  }

  @Override
public void canUndoOverDoc(ReceiptRegDocVO vo) throws LfwBusinessException
  {
    try {
      ReceiptRegDocVO[] vos = getReceiptRegDocVOsBySql(String.format(" where pk_receiptregdoc = '%s' ", new Object[] { vo.getPk_receiptregdoc() }));
      if ((vos != null) && (vos.length > 0))
        vo = vos[0];
    } catch (BusinessException e1) {
      throw new LfwRuntimeException(e1.getMessage());
    }
    if (!(vo.getIsover().booleanValue())) {
      throw new LfwBusinessException(NCLangResOnserver.getInstance().getStrByID("odreceiptdocmgr", "ReceiptRegDocServiceImpl-000009"));
    }

    if (vo.getIsarchive().booleanValue())
      throw new LfwBusinessException(NCLangResOnserver.getInstance().getStrByID("odreceiptdocmgr", "ReceiptRegDocServiceImpl-000010"));
  }

  private boolean isOverOrArchive(ReceiptRegDocVO vo)
  {
    return ((!(vo.getIsover().booleanValue())) && (!(vo.getIsarchive().booleanValue())));
  }

  @Override
public void archiveDoc(ReceiptRegDocVO vo)
    throws LfwBusinessException
  {
    canArchiveDoc(vo);

    IArchiveService service = NCLocator.getInstance().lookup(IArchiveService.class);
    try
    {
      service.addArchive(getArchiveParam(vo));
    } catch (BusinessException e) {
      throw new LfwBusinessException(e);
    }
  }

  @Override
public void afterArchiveProcess(ReceiptRegDocVO vo)
  {
    vo.setIsarchive(UFBoolean.TRUE);
    if (vo.getStatus() == 0)
      vo.setStatus(1);
    try {
      save(vo);
    }
    catch (BusinessException e) {
      throw new LfwRuntimeException(e);
    }
  }

  @Override
public void canArchiveDoc(ReceiptRegDocVO vo) throws LfwBusinessException
  {
    OAODArchiveHelper.checkArchStart();
    if (!(vo.getIsover().booleanValue())) {
      throw new LfwBusinessException(NCLangResOnserver.getInstance().getStrByID("odreceiptdocmgr", "ReceiptRegDocServiceImpl-000011"));
    }

    if (vo.getIsarchive().booleanValue())
      throw new LfwBusinessException(NCLangResOnserver.getInstance().getStrByID("odreceiptdocmgr", "ReceiptRegDocServiceImpl-000012"));
  }

  @Override
public ReceiptRegDocVO undoArchiveDoc(ReceiptRegDocVO vo)
    throws LfwBusinessException
  {
    canUndoArchiveDoc(vo);
    IArchiveService service = NCLocator.getInstance().lookup(IArchiveService.class);
    try
    {
      service.cancelArchive(vo.getDoctype(), vo.getPk_receiptregdoc());
    } catch (BusinessException e) {
      throw new LfwRuntimeException(e);
    }
    vo.setIsarchive(UFBoolean.FALSE);
    if (vo.getStatus() == 0)
      vo.setStatus(1);
    try {
      return save(vo);
    } catch (BusinessException e) {
      throw new LfwBusinessException(e.getMessage());
    }
  }

  public void save(ReceiptRegDocVO[] vos) throws BusinessException {
    if ((vos == null) || (vos.length == 0)) return;
    List newList = new ArrayList();
    List updateList = new ArrayList();
    List deleteList = new ArrayList();
    for (ReceiptRegDocVO vo : vos) {
      if (vo.getStatus() == 2)
      {
        newList.add(vo);
      }
      else if (vo.getStatus() == 1)
      {
        vo.setTs(null);
        updateList.add(vo);
      } else if (vo.getStatus() == 3) {
        deleteList.add(vo);
      }
    }
    IUifCpbService service = NCLocator.getInstance().lookup(IUifCpbService.class);
    if (newList.size() > 0) {
      service.insertSuperVOs((SuperVO[])newList.toArray(new ReceiptRegDocVO[0]), false);
    }
    if (updateList.size() > 0)
    {
      CRUDHelper.getMdCRUDService().saveBusinessVOs(updateList.toArray(new ReceiptRegDocVO[0]));
    }
    if (deleteList.size() > 0)
      service.deleteSuperVOs((SuperVO[])deleteList.toArray(new ReceiptRegDocVO[0]), false, true);
  }

  @Override
public void undoRealArchiveDoc(ReceiptRegDocVO[] vos)
    throws LfwBusinessException
  {
    IArchiveService service = NCLocator.getInstance().lookup(IArchiveService.class);

    List pks = new ArrayList();
    List pk_types = new ArrayList();
    try {
      canUndoArchiveDocs(vos);
      service.canCancelArchiveBatch((String[])pk_types.toArray(new String[pk_types.size()]), (String[])pks.toArray(new String[pks.size()]));
      for (ReceiptRegDocVO vo : vos) {
        pks.add(vo.getPk_receiptregdoc());
        pk_types.add(vo.getDoctype());
        vo.setIsarchive(UFBoolean.FALSE);
        if (vo.getStatus() == 0)
          vo.setStatus(1);
      }
      save(vos);
    } catch (BusinessException e) {
      throw new LfwRuntimeException(e);
    }
  }

  @Override
public void canUndoArchiveDoc(ReceiptRegDocVO vo)
    throws LfwBusinessException
  {
    OAODArchiveHelper.checkArchStart();
    if (!(vo.getIsarchive().booleanValue())) {
      throw new LfwBusinessException(NCLangResOnserver.getInstance().getStrByID("odreceiptdocmgr", "ReceiptRegDocServiceImpl-000013"));
    }

    IArchiveService service = NCLocator.getInstance().lookup(IArchiveService.class);
    try
    {
      if (!(service.canCancelArchive(vo.getDoctype(), vo.getPk_receiptregdoc()).booleanValue()))
      {
        throw new LfwRuntimeException(NCLangResOnserver.getInstance().getStrByID("odreceiptdocmgr", "ReceiptRegDocServiceImpl-000015"));
      }
    }
    catch (BusinessException e)
    {
      throw new LfwRuntimeException(e);
    }
  }

  public void canUndoArchiveDocs(ReceiptRegDocVO[] vos) throws LfwBusinessException {
    OAODArchiveHelper.checkArchStart();
    IArchiveService service = NCLocator.getInstance().lookup(IArchiveService.class);

    List pks = new ArrayList();
    List pk_types = new ArrayList();
    for (ReceiptRegDocVO vo : vos) {
      if (!(vo.getIsarchive().booleanValue())) {
        throw new LfwBusinessException(NCLangResOnserver.getInstance().getStrByID("odreceiptdocmgr", "ReceiptRegDocServiceImpl-000013"));
      }

      pks.add(vo.getPk_receiptregdoc());
      pk_types.add(vo.getDoctype());
    }
    try
    {
      if (!(service.canCancelArchiveBatch((String[])pk_types.toArray(new String[pk_types.size()]), (String[])pks.toArray(new String[pks.size()])).booleanValue()))
      {
        throw new LfwRuntimeException(NCLangResOnserver.getInstance().getStrByID("odreceiptdocmgr", "ReceiptRegDocServiceImpl-000015"));
      }
    }
    catch (BusinessException e)
    {
      throw new LfwRuntimeException(e);
    }
  }

  @Override
public boolean getReceiptRegDocCanChange(String regKey) {
    ReceiptRegDocVO regvo;
    IReceiptRegDocService regsrv = NCLocator.getInstance().lookup(IReceiptRegDocService.class);
    try
    {
      regvo = regsrv.getVOByPk(regKey);
    }
    catch (BusinessException e) {
      throw new LfwRuntimeException(e.getMessage());
    }

    return ((regvo.getBillstatus().equals(BillStatusEnum.END.value().toString())) || (regvo.getBillstatus().equals(BillStatusEnum.CANCELLATION.value().toString())) || (regvo.getIsarchive().booleanValue()) || (regvo.getIsover().booleanValue()));
  }

  @Override
public void canAddDealDoc(String regKey)
    throws LfwBusinessException
  {
    if (!(getReceiptRegDocCanChange(regKey)))
      throw new LfwBusinessException(NCLangResOnserver.getInstance().getStrByID("odreceiptdocmgr", "ReceiptRegDocServiceImpl-000014"));
  }

  @Override
public ReceiptRegDocVO canAddDealDoc(ReceiptRegDocVO vo)
    throws LfwBusinessException
  {
    try
    {
      vo = save(vo);
    }
    catch (BusinessException e) {
      throw new LfwRuntimeException(e.getMessage());
    }
    canAddDealDoc(vo.getPk_receiptregdoc());
    return vo;
  }

  @Override
public Map<String, String> getArchiveParam(ReceiptRegDocVO regvo)
  {
    Map params = new HashMap();
    params.put("pk_docsource", regvo.getPk_receiptregdoc());

    params.put("pk_from_group", regvo.getPk_group());
    params.put("pk_from_org", regvo.getPk_org());
    params.put("archiveyear", Integer.toString(regvo.getCreationtime().getYear()));

    params.put("title", regvo.getTitle());
    params.put("doccode", (StringUtils.isEmpty(regvo.getRpmark())) ? regvo.getDispatchno() : regvo.getRpmark());
    try
    {
      PsndocVO psn = PersonUtil.getPersonByUser(regvo.getCreator());
      params.put("docauthor", psn.getPk_psndoc());
      if ((psn.getPsnjobs() != null) && (psn.getPsnjobs().length > 0)) {
        for (PsnjobVO job : psn.getPsnjobs()) {
          if (job.getIsmainjob().booleanValue())
            params.put("pk_dept", job.getPk_dept());
        }
      }
    }
    catch (BusinessException e)
    {
      throw new LfwRuntimeException(e);
    }

    FileManager filemgr = OAODUtil.getSystemFileManagerByBafile();
    int countfile = 0;
    LfwFileVO[] files = null;
    try
    {
      files = filemgr.getAttachFileByItemID(regvo.getFileitem());

      if (files != null) {
        countfile += files.length;
      }
      params.put("attachcount", Integer.toString(countfile));
    }
    catch (BusinessException e)
    {
      throw new LfwRuntimeException(e);
    }

    params.put("keyword", regvo.getSubject());
    params.put("pk_archivebusinesstype", "0001ZC1000000oaodreg");

    params.put("pk_archivebusinesscat", regvo.getDoctype());

    return params;
  }

  @Override
public ReceiptRegDocVO[] getReceiptRegDocVOsBySql(String whereSql)
    throws BusinessException
  {
    ReceiptRegDocVO[] vos = null;
    try {
      vos = CRUDHelper.getMdCRUDService().queryVOs(String.format("select * from  oaod_receiptregdoc %s", new Object[] { whereSql }), ReceiptRegDocVO.class, null, "creationtime desc", null);
    }
    catch (LfwBusinessException e)
    {
      throw new LfwRuntimeException(e);
    }
    return vos;
  }
}