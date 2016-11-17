package nc.impl.oa.oaod.officialdoctype;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.ml.NCLangResOnserver;
import nc.bs.oa.oaco.basecomp.scopeset.ScopeSetUtil;
import nc.bs.oa.oaco.pub.base.OaSuperVOService;
import nc.bs.oa.oapub.helper.SQLHelper;
import nc.bs.uif2.validation.Validator;
import nc.itf.oa.oaod.officialdoctype.IOfficialDocTypeService;
import nc.md.IMDQueryFacade;
import nc.md.MDBaseQueryFacade;
import nc.md.model.IAttribute;
import nc.md.model.IBean;
import nc.md.model.MetaDataException;
import nc.uap.cpb.org.exception.CpbBusinessException;
import nc.uap.cpb.org.itf.ICpUserQry;
import nc.uap.cpb.org.vos.CpUserVO;
import nc.uap.cpb.templaterela.itf.ITemplateRelationQryService;
import nc.uap.ctrl.pa.itf.IPaPublicQryService;
import nc.uap.ctrl.tpl.exp.TplBusinessException;
import nc.uap.ctrl.tpl.print.ICpPrintTemplateInnerQryService;
import nc.uap.ctrl.tpl.print.ICpPrintTemplateOuterService;
import nc.uap.ctrl.tpl.print.base.CpPrintConditionVO;
import nc.uap.lfw.core.LfwRuntimeEnvironment;
import nc.uap.lfw.core.data.PaginationInfo;
import nc.uap.lfw.core.exception.LfwBusinessException;
import nc.uap.lfw.core.exception.LfwRuntimeException;
import nc.uap.lfw.core.log.LfwLogger;
import nc.uap.lfw.crud.itf.ILfwQueryService;
import nc.uap.lfw.file.FileManager;
import nc.uap.lfw.pa.PaBusinessException;
import nc.uap.lfw.pa.PaServiceFacility;
import nc.uap.lfw.stylemgr.vo.UwTemplateVO;
import nc.uap.wfm.exception.WfmServiceException;
import nc.uap.wfm.itf.IWfmFlwTypeBill;
import nc.uap.wfm.itf.IWfmFlwTypeQry;
import nc.uap.wfm.model.ProDef;
import nc.uap.wfm.utils.WfmUtilFacade;
import nc.uap.wfm.vo.WfmFlwTypeVO;
import nc.util.oa.oaod.OAODUtil;
import nc.vo.ml.Language;
import nc.vo.ml.LanguageTranslatorFactor;
import nc.vo.ml.LanguageVO;
import nc.vo.ml.MultiLangContext;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.ml.translator.ITranslator;
import nc.vo.oa.oaod.commonenum.OfficialDocCategoryEnum;
import nc.vo.oa.oaod.commonenum.OfficialDocEnum;
import nc.vo.oa.oaod.officialdoctype.OfficialDocTypeVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.querytemplate.md.MDTemplateUtil;
import nc.vo.util.BDUniqueRuleValidate;
import nc.vo.util.VisibleUtil;
import org.apache.commons.lang.StringUtils;

public class OfficialDocTypeServiceImpl extends OaSuperVOService<OfficialDocTypeVO>
  implements IOfficialDocTypeService
{
  @Override
protected Validator[] getInsertValidator()
  {
    return new Validator[] { new BDUniqueRuleValidate() };
  }

  @Override
protected Validator[] getUpdateValidator(OfficialDocTypeVO oldVO, OfficialDocTypeVO newVO)
  {
    return new Validator[] { new BDUniqueRuleValidate() };
  }

  public OfficialDocTypeServiceImpl() {
    super("42d3c580-4a4d-4c01-82cb-53586bafb391", OfficialDocTypeVO.class, null);
  }

  @Override
public OfficialDocTypeVO getNewDocType(String pk_org, String category)
    throws BusinessException
  {
    OfficialDocTypeVO vo = new OfficialDocTypeVO();
    vo.setPk_group(InvocationInfoProxy.getInstance().getGroupId());
    vo.setPk_org(pk_org);
    vo.setStatus(2);
    vo.setCategory(category);
    return vo;
  }

  private String getName(OfficialDocTypeVO vo, int seq) {
    String name = vo.getName();
    switch (seq)
    {
    case 0:
      name = vo.getName();
      break;
    case 1:
      name = vo.getName2();
      break;
    case 2:
      name = vo.getName3();
      break;
    case 3:
      name = vo.getName4();
      break;
    case 4:
      name = vo.getName5();
      break;
    case 5:
      name = vo.getName6();
    }

    return name;
  }

  @Override
public OfficialDocTypeVO save(OfficialDocTypeVO vo)
  {
    OfficialDocTypeVO newvo;
    try
    {
      LanguageVO[] langs = MultiLangContext.getInstance().getEnableLangVOs();

      String[] strRegdocs = new String[langs.length];
      String[] strDealdocs = new String[langs.length];
      String[] regtypenames = { null, null, null, null, null, null };

      String[] dealtypenames = { null, null, null, null, null, null };

      for (int i = 0; i < langs.length; ++i) {
        ITranslator translator = null;
        try {
          Language language = NCLangResOnserver.getInstance().getLanguage(langs[i].getLangcode());

          if (language != null) {
            translator = LanguageTranslatorFactor.getInstance().getTranslator(language);
          }
        }
        catch (Exception e)
        {
        }
        if (translator != null) {
          strRegdocs[i] = translator.getString("odbasicsetting", null, NCLangRes4VoTransl.getNCLangRes().getStrByID("odbasicsetting", "OfficialDocTypeServiceImpl-000001"));

          strDealdocs[i] = translator.getString("odbasicsetting", null, "-处理单");

          if (strRegdocs[i] == null) {
            strRegdocs[i] = "-登记单";
          }
          if (strDealdocs[i] == null) {
            strDealdocs[i] = "-处理单";
          }
          regtypenames[i] = getName(vo, i) + strRegdocs[i];

          dealtypenames[i] = getName(vo, i) + strDealdocs[i];
        }
      }

      switch (vo.getStatus())
      {
      case 2:
        if (vo.getCategory().endsWith("Send"))
        {
          WfmFlwTypeVO sendflwtype = NewFlwTypeVO(vo.getPk_org(), UUID.randomUUID().toString(), vo.getName(), vo.getName2(), vo.getName3(), vo.getName4(), vo.getName5(), vo.getName6(), "0000ZC1000000WFMSEND", "0001ZC1000000000OAOD", "0001ZC1000000003MUO5", "nc.impl.oa.oaod.senddoc.SendDocWfmFormOperImpl");

          vo.setDispatchflwtype(sendflwtype.getPk_flwtype());

          addPrintTemplate(sendflwtype.getPk_flwtype(), vo.getName() + NCLangResOnserver.getInstance().getStrByID("odbasicsetting", "OfficialDocTypeServiceImpl-000000"), "853d4e9c-4440-472f-94bf-9b408c00b981", "E3280401", sendflwtype.getPk_flwtype(), vo.getPk_group(), vo.getPk_org());
        }
        else if (vo.getCategory().endsWith("Receipt"))
        {
          WfmFlwTypeVO regflwtype = NewFlwTypeVO(vo.getPk_org(), UUID.randomUUID().toString(), regtypenames[0], regtypenames[1], regtypenames[2], regtypenames[3], regtypenames[4], regtypenames[5], "0000ZC10000000WFMREG", "0001ZC1000000000OAOD", "0001ZC1000000001VPIQ", "nc.impl.oa.oaod.receiptregdoc.ReceiptRegDocWfmFormOperImpl");

          vo.setReceiptflwtype(regflwtype.getPk_flwtype());
          addPrintTemplate(regflwtype.getPk_flwtype(), vo.getName() + NCLangResOnserver.getInstance().getStrByID("odbasicsetting", "OfficialDocTypeServiceImpl-000001"), "4359b387-e6bc-4199-b52a-a47dcbb6ad99", "E3280701", regflwtype.getPk_flwtype(), vo.getPk_group(), vo.getPk_org());

          WfmFlwTypeVO dealflwtype = NewFlwTypeVO(vo.getPk_org(), UUID.randomUUID().toString(), dealtypenames[0], dealtypenames[1], dealtypenames[2], dealtypenames[3], dealtypenames[4], dealtypenames[5], "0000ZC1000000WFMDEAL", "0001ZC1000000000OAOD", "0001ZC1000000002GIRM", "nc.impl.oa.oaod.receiptdealdoc.ReceiptDealDocWfmFormOperImpl");

          vo.setDealflwtype(dealflwtype.getPk_flwtype());
          addPrintTemplate(dealflwtype.getPk_flwtype(), vo.getName() + NCLangResOnserver.getInstance().getStrByID("odbasicsetting", "OfficialDocTypeServiceImpl-000002"), "9cf6b4d7-4abf-4ff7-88af-c72faaecd790", "E3280704", dealflwtype.getPk_flwtype(), vo.getPk_group(), vo.getPk_org());
        }

        newvo = insert(vo);
        break;
      case 1:
        if (vo.getCategory().endsWith("Send")) {
          UpdateFlwTypeVO(vo.getDispatchflwtype(), UUID.randomUUID().toString(), vo.getName(), vo.getName2(), vo.getName3(), vo.getName4(), vo.getName5(), vo.getName6());
        }
        else if (vo.getCategory().endsWith("Receipt"))
        {
          UpdateFlwTypeVO(vo.getReceiptflwtype(), UUID.randomUUID().toString(), regtypenames[0], regtypenames[1], regtypenames[2], regtypenames[3], regtypenames[4], regtypenames[5]);

          UpdateFlwTypeVO(vo.getDealflwtype(), UUID.randomUUID().toString(), dealtypenames[0], dealtypenames[1], dealtypenames[2], dealtypenames[3], dealtypenames[4], dealtypenames[5]);
        }

        newvo = update(vo);
        break;
      case 3:
        if (vo.getCategory().endsWith("Send")) {
          DeleteFlwTypeVO(vo.getDispatchflwtype());
          delUwTemplate("0001ZC1000000003MUO2", "NewSendDocApp", "SendDocWin", vo.getDispatchflwtype(), vo);

          deletePrintTempalte("E3280401", vo.getDispatchflwtype());
        } else if (vo.getCategory().endsWith("Receipt"))
        {
          DeleteFlwTypeVO(vo.getReceiptflwtype());
          delUwTemplate("0001ZC1000000001VPIJ", "ReceiptRegApp", "ReceiptRegDoc", vo.getReceiptflwtype(), vo);

          deletePrintTempalte("E3280701", vo.getReceiptflwtype());

          DeleteFlwTypeVO(vo.getDealflwtype());
          delUwTemplate("0001ZC1000000002GIRH", "ReceiptDealApp", "ReceiptDealDoc", vo.getDealflwtype(), vo);

          deletePrintTempalte("E3280704", vo.getDealflwtype());
        }
        delete(vo);
        newvo = null;
        break;
      default:
        newvo = vo;
      }
    }
    catch (BusinessException e) {
      throw new LfwRuntimeException(e);
    }
    return newvo;
  }

  private void addPrintTemplate(String modelcode, String modelname, String metaclass, String nodecode, String nodekey, String pk_group, String pk_org)
    throws TplBusinessException, MetaDataException
  {
    ICpPrintTemplateOuterService printservice = NCLocator.getInstance().lookup(ICpPrintTemplateOuterService.class);

    ICpPrintTemplateInnerQryService printqry = NCLocator.getInstance().lookup(ICpPrintTemplateInnerQryService.class);

    String pk_file = printqry.getSysPrintTemplateVOPkByNode(nodecode, null).getPk_file();
    try
    {
      FileManager filemgr = OAODUtil.getSystemFileManagerByDefault();
      pk_file = filemgr.copyFile(NCLangResOnserver.getInstance().getStrByID("odbasicsetting", "OfficialDocTypeServiceImpl-000003"), null, null, null, pk_file);
    }
    catch (Exception e)
    {
      throw new LfwRuntimeException(e);
    }

    printservice.addPrintTemplate(modelcode, modelname, metaclass + ",0f07e9dc-f441-4bb2-821c-2b1b9b8b484e", nodecode, nodekey, pk_file, null, true, pk_group, pk_org);
  }

  private void addPrintConditionFromBean(String metaclass, List<CpPrintConditionVO> prtconds, String tabletype)
    throws MetaDataException
  {
    IMDQueryFacade bo = MDBaseQueryFacade.getInstance();
    IBean bean = bo.getBeanByID(metaclass);
    String tablecode = bean.getTable().getName();
    for (IAttribute attr : bean.getAttributes())
      if ((!(attr.getName().equalsIgnoreCase("ts"))) && (!(attr.getName().equalsIgnoreCase("dr"))) && (!(attr.getName().equalsIgnoreCase("vostatus"))) && (!(attr.getName().equalsIgnoreCase(bean.getPrimaryKey().getPKColumn().getName()))))
      {
        int datatype;
        if ((metaclass.equalsIgnoreCase("853d4e9c-4440-472f-94bf-9b408c00b981")) && (attr.getName().equalsIgnoreCase("creationtime")))
        {
          datatype = 3;
        }
        else datatype = MDTemplateUtil.getQTDataType(attr.getDataType().getTypeType());

        CpPrintConditionVO prtcond = createPrintCondition(tabletype, attr, datatype, String.format("%s.%s", new Object[] { tablecode, attr.getName() }), attr.getDisplayName(), tablecode);

        prtconds.add(prtcond);
      }
  }

  private CpPrintConditionVO createPrintCondition(String tabletype, IAttribute attr, int datatype, String varexpress, String displayname, String tablecode)
  {
    CpPrintConditionVO prtcond = new CpPrintConditionVO();
    prtcond.setDatatype(Integer.valueOf(datatype));
    prtcond.setTabletype(tabletype);
    prtcond.setVartype("MD");
    prtcond.setVarexpress(varexpress);
    prtcond.setVarname(displayname);
    prtcond.setResid(attr.getResID());
    prtcond.setTabcode(tablecode);
    return prtcond;
  }

  private void deletePrintTempalte(String nodecode, String nodekey) throws TplBusinessException
  {
    ICpPrintTemplateOuterService printservice = NCLocator.getInstance().lookup(ICpPrintTemplateOuterService.class);

    printservice.deletePrintTempalte(nodecode, nodekey);
  }

  private void delUwTemplate(String pk_funcnode, String appid, String winid, String flwtype, OfficialDocTypeVO vo)
  {
    try
    {
      PaServiceFacility.getPaService().removeTemplateByDimensions(OAODUtil.getDimensions(pk_funcnode, appid, winid, flwtype, null, null));
    }
    catch (PaBusinessException e)
    {
      throw new LfwRuntimeException(e);
    }
  }

  private WfmFlwTypeVO NewFlwTypeVO(String org, String code, String name, String name2, String name3, String name4, String name5, String name6, String parent, String flwCat, String pageid, String serverclass)
    throws LfwRuntimeException
  {
    IWfmFlwTypeBill flwtypesrv = NCLocator.getInstance().lookup(IWfmFlwTypeBill.class);

    WfmFlwTypeVO flwtype = new WfmFlwTypeVO();
    flwtype.setPk_group(InvocationInfoProxy.getInstance().getGroupId());
    if (!(InvocationInfoProxy.getInstance().getGroupId().equals(org)))
      flwtype.setPk_org(org);
    else
      flwtype.setPk_org(flwtype.getPk_group());
    flwtype.setTypecode(code);
    flwtype.setTypename(name);
    flwtype.setTypename2(name2);
    flwtype.setTypename3(name3);
    flwtype.setTypename4(name4);
    flwtype.setTypename5(name5);
    flwtype.setTypename6(name6);
    flwtype.setPk_parent(parent);
    flwtype.setPk_flwcat(flwCat);
    flwtype.setPageid(pageid);
    flwtype.setServerclass(serverclass);
    flwtype.setSeeflag(UFBoolean.valueOf(true));

    flwtype.setStatus(2);
    try {
      flwtypesrv.saveFlwType(flwtype);
    } catch (WfmServiceException e) {
      throw new LfwRuntimeException(e);
    }
    return flwtype;
  }

  private void UpdateFlwTypeVO(String flwtypepk, String code, String name, String name2, String name3, String name4, String name5, String name6)
    throws LfwRuntimeException
  {
    IWfmFlwTypeQry flwtypeqrysrv = NCLocator.getInstance().lookup(IWfmFlwTypeQry.class);

    IWfmFlwTypeBill flwtypesrv = NCLocator.getInstance().lookup(IWfmFlwTypeBill.class);

    WfmFlwTypeVO flwtypevo = flwtypeqrysrv.getFlwTypeVoByPk(flwtypepk);
    if (flwtypevo == null) {
      throw new LfwRuntimeException(NCLangResOnserver.getInstance().getStrByID("odbasicsetting", "OfficialDocTypeServiceImpl-000004"));
    }

    flwtypevo.setTypecode(code);
    flwtypevo.setTypename(name);
    flwtypevo.setTypename2(name2);
    flwtypevo.setTypename3(name3);
    flwtypevo.setTypename4(name4);
    flwtypevo.setTypename5(name5);
    flwtypevo.setTypename6(name6);
    flwtypevo.setStatus(1);
    try {
      flwtypesrv.saveFlwType(flwtypevo);
    } catch (WfmServiceException e) {
      throw new LfwRuntimeException(e);
    }
  }

  private void DeleteFlwTypeVO(String flwtypepk)
    throws LfwRuntimeException
  {
    IWfmFlwTypeBill flwtypesrv = NCLocator.getInstance().lookup(IWfmFlwTypeBill.class);
    try
    {
      flwtypesrv.deleteFlwTypeByPk(flwtypepk);
    } catch (WfmServiceException e) {
      throw new LfwRuntimeException(e);
    }
  }

  private OfficialDocTypeVO insert(OfficialDocTypeVO vo) throws LfwRuntimeException
  {
    try {
      return (super.doSave(vo));
    } catch (BusinessException e) {
      throw new LfwRuntimeException(e);
    }
  }

  private OfficialDocTypeVO update(OfficialDocTypeVO vo) throws LfwRuntimeException
  {
    try {
      return (super.doSave(vo));
    } catch (BusinessException e) {
      throw new LfwRuntimeException(e);
    }
  }

  private void delete(OfficialDocTypeVO vo) throws LfwRuntimeException {
    try {
      super.doSave(vo);
    } catch (BusinessException e) {
      throw new LfwRuntimeException(e);
    }
  }

  @Override
public OfficialDocTypeVO queryByKey(String primaryKey)
    throws BusinessException
  {
    Collection vos = getOaQueryService().queryBillOfVOByCond(OfficialDocTypeVO.class, String.format(" pk_type = '%s' ", new Object[] { primaryKey }), false);

    if ((vos != null) && (vos.size() > 0)) {
      return ((OfficialDocTypeVO[])vos.toArray(new OfficialDocTypeVO[vos.size()]))[0];
    }
    return null;
  }

  @Override
public OfficialDocTypeVO[] queryByWhere(String sqlWhere, String sqlOrder, PaginationInfo pg)
    throws BusinessException
  {
    OfficialDocTypeVO[] vos = null;
    try
    {
      vos = getOaQueryService().queryVOs(new OfficialDocTypeVO(), pg, sqlWhere, null, sqlOrder);
    }
    catch (Exception e) {
      throw new LfwRuntimeException(e);
    }
    return vos;
  }

  @Override
public OfficialDocTypeVO[] queryByWhere(String sqlWhere, PaginationInfo pg)
    throws BusinessException
  {
    return queryByWhere(sqlWhere, "", pg);
  }

  @Override
public ProDef getCurrentUserProdef(String odtypepk, OfficialDocEnum docenum, String curorg)
  {
    try
    {
      if (queryByKeyLfw(odtypepk) == null) {
        return null;
      }
      return getCurrentUserProdef(queryByKeyLfw(odtypepk), docenum, curorg);
    } catch (BusinessException e) {
      throw new LfwRuntimeException(e);
    }
  }

  @Override
public ProDef getCurrentUserProdef(OfficialDocTypeVO vo, OfficialDocEnum docenum, String curorg)
  {
    String pk_flwtype;
    if (vo == null) {
      return null;
    }
    if (docenum.equals(OfficialDocEnum.RECEIPTREGDOC))
      pk_flwtype = vo.getReceiptflwtype();
    else if (docenum.equals(OfficialDocEnum.RECEIPTDEALDOC))
      pk_flwtype = vo.getDealflwtype();
    else if (docenum.equals(OfficialDocEnum.SENDDOC))
      pk_flwtype = vo.getDispatchflwtype();
    else {
      throw new LfwRuntimeException(NCLangResOnserver.getInstance().getStrByID("odbasicsetting", "OfficialDocTypeServiceImpl-000005"));
    }
/*=================================修改========================================*/
    new WfmUtilFacade();
    ProDef prodef = null;
    if(LfwRuntimeEnvironment.getLfwSessionBean() == null||LfwRuntimeEnvironment.getLfwSessionBean().getPk_user() == null){
    	prodef = (ProDef)WfmUtilFacade.getProDefByFlowType(pk_flwtype,InvocationInfoProxy.getInstance().getUserId(), curorg, InvocationInfoProxy.getInstance().getGroupId());
    }else{
    	prodef = (ProDef)WfmUtilFacade.getProDefByFlowType(pk_flwtype, LfwRuntimeEnvironment.getLfwSessionBean().getPk_user(), curorg, InvocationInfoProxy.getInstance().getGroupId());
    }
    
    if ((prodef == null) || (StringUtils.isEmpty(prodef.getPk_prodef()))) {
      if (vo.getCategory().equalsIgnoreCase(OfficialDocCategoryEnum.RECEIPT.value().toString()))
      {
        throw new LfwRuntimeException(String.format(NCLangResOnserver.getInstance().getStrByID("odbasicsetting", "OfficialDocTypeServiceImpl-000006"), new Object[] { SQLHelper.getMuiltiLangValue(vo, "name") }));
      }

      throw new LfwRuntimeException(String.format(NCLangResOnserver.getInstance().getStrByID("odbasicsetting", "OfficialDocTypeServiceImpl-000007"), new Object[] { SQLHelper.getMuiltiLangValue(vo, "name") }));
    }

    return prodef;
  }

  @Override
public String getDefaultPersonalTemplatePk(OfficialDocEnum docenum, String flwtype)
  {
    IPaPublicQryService service = NCLocator.getInstance().lookup(IPaPublicQryService.class);
    try
    {
      String cond = " appid = '%s' and windowid = '%s' and busiid = '%s' and pk_prodef = '~' and port_id = '~' ";
      if (docenum.equals(OfficialDocEnum.RECEIPTREGDOC)) {
        cond = String.format(cond, new Object[] { "ReceiptRegApp", "ReceiptRegDoc", flwtype });
      }
      else if (docenum.equals(OfficialDocEnum.RECEIPTDEALDOC)) {
        cond = String.format(cond, new Object[] { "ReceiptDealApp", "ReceiptDealDoc", flwtype });
      }
      else {
        cond = String.format(cond, new Object[] { "NewSendDocApp", "SendDocWin", flwtype });
      }
      Collection tmps = service.getTemplateVOByCondition(cond);

      if ((tmps != null) && (tmps.size() > 0)) {
        Iterator i$ = tmps.iterator(); if (i$.hasNext()) { UwTemplateVO tmp = (UwTemplateVO)i$.next();
          return tmp.getPk_template(); }
      }
    } catch (PaBusinessException e) {
      throw new LfwRuntimeException(e);
    } catch (LfwBusinessException e) {
      throw new LfwRuntimeException(e);
    }
    return null;
  }

  @Override
public OfficialDocTypeVO[] getOfficialDocTypeByUser(OfficialDocCategoryEnum category, String pk_adminorg, PaginationInfo pg)
  {
    String whereSql = "";
    try {
      whereSql = whereSql + String.format(" category='%s' and %s and pk_type in %s ", new Object[] { category.value().toString(), VisibleUtil.getRefVisibleCondition(InvocationInfoProxy.getInstance().getGroupId(), pk_adminorg, "42d3c580-4a4d-4c01-82cb-53586bafb391"), ScopeSetUtil.getScopeSetWhereSql(LfwRuntimeEnvironment.getLfwSessionBean().getPk_user(), "officialdoctype", "mgrscopeid", true, true, true, true) });

      return queryByWhere(whereSql, " order by creationtime desc ", pg);
    } catch (LfwBusinessException e) {
      throw new LfwRuntimeException(e);
    } catch (BusinessException e) {
      throw new LfwRuntimeException(e);
    }
  }

  @Override
public OfficialDocTypeVO[] getOfficialDocTypeByUserOrderByCodeAsc(OfficialDocCategoryEnum category, String pk_adminorg, PaginationInfo pg)
  {
//    OfficialDocTypeVO[] vos = null;
    String whereSql = "";
    try {
      whereSql = whereSql + String.format(" category='%s' and %s and pk_type in %s ", new Object[] { category.value().toString(), VisibleUtil.getRefVisibleCondition(InvocationInfoProxy.getInstance().getGroupId(), pk_adminorg, "42d3c580-4a4d-4c01-82cb-53586bafb391"), ScopeSetUtil.getScopeSetWhereSql(LfwRuntimeEnvironment.getLfwSessionBean().getPk_user(), "officialdoctype", "mgrscopeid", true, true, true, true) });

      return queryByWhere(whereSql, " order by code asc ", pg);
    }
    catch (LfwBusinessException e) {
      throw new LfwRuntimeException(e);
    }
    catch (BusinessException e) {
      throw new LfwRuntimeException(e);
    }
  }

  @Override
public String getInfoMobileAuditTemplatePK(String userId, String deviceType, String appId, String winId)
    throws BusinessException
  {
    String[] tmpPKs = getInfoMobileTemplatePKs(userId, appId, winId, deviceType);

    ICpUserQry cpUserQry = NCLocator.getInstance().lookup(ICpUserQry.class);
    CpUserVO uservo = null;
    try {
      uservo = cpUserQry.getUserByPk(userId);
    } catch (CpbBusinessException e) {
      throw new LfwRuntimeException(e);
    }

    ITemplateRelationQryService qryService = NCLocator.getInstance().lookup(ITemplateRelationQryService.class);

    String pk_template = qryService.filterTemplateByUser(uservo, tmpPKs);
    if (pk_template == null) {
      throw new LfwRuntimeException(NCLangResOnserver.getInstance().getStrByID("odbasicsetting", "OfficialDocTypeServiceImpl-000008"));
    }

    return pk_template;
  }

  private String[] getInfoMobileTemplatePKs(String userId, String appId, String winId, String deviceType) throws BusinessException
  {
    IPaPublicQryService service = NCLocator.getInstance().lookup(IPaPublicQryService.class);

    String cond = OAODUtil.buildSqlBak(" appid='?' and windowid='?' and ISACTIVE='Y' and pk_device in(select pk_device from cp_device where code = '?')", new String[] { appId, winId, deviceType });

    Collection<UwTemplateVO> tmps = service.getTemplateVOByCondition(cond);
    if ((tmps == null) || (tmps.size() == 0)) {
      return null;
    }
    String[] tempPKs = new String[tmps.size()];
    int i = 0;
    for (UwTemplateVO tmp : tmps) {
      tempPKs[i] = tmp.getPk_template();
      ++i;
    }

    return tempPKs;
  }

  @Override
public OfficialDocTypeVO queryByKeyLfw(String primaryKey)
    throws BusinessException
  {
    OfficialDocTypeVO[] vos = null;
    String whereSql = String.format(" pk_type = '%s' ", new Object[] { primaryKey });
    ILfwQueryService queryService = NCLocator.getInstance().lookup(ILfwQueryService.class);
    try {
      vos = queryService.queryVOs(whereSql, OfficialDocTypeVO.class, null, null);
    } catch (BusinessException e) {
      LfwLogger.error(e.getMessage());
    }
    if ((vos != null) && (vos.length > 0)) {
      return vos[0];
    }

    return null;
  }
}