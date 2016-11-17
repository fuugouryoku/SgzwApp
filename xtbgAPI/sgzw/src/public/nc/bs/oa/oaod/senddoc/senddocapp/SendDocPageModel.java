package nc.bs.oa.oaod.senddoc.senddocapp;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.oa.oaco.basecomp.associateform.AssociateFormUtil;
import nc.bs.oa.oaod.senddoc.SendDocUtil;
import nc.bs.oa.oaod.senddocbyreg.SendDocByRegData;
import nc.bs.oa.oapub.wfm.OAWfmUtil;
import nc.itf.oa.oaod.officialdoctype.IOfficialDocTypeService;
import nc.uap.ctrl.file.DefaultFileMgrView;
import nc.uap.lfw.app.plugin.AppControlPlugin;
import nc.uap.lfw.core.AppInteractionUtil;
import nc.uap.lfw.core.LfwRuntimeEnvironment;
import nc.uap.lfw.core.combodata.CombItem;
import nc.uap.lfw.core.combodata.ComboData;
import nc.uap.lfw.core.comp.FormComp;
import nc.uap.lfw.core.comp.MenuItem;
import nc.uap.lfw.core.comp.MenubarComp;
import nc.uap.lfw.core.ctx.AppLifeCycleContext;
import nc.uap.lfw.core.event.TabEvent;
import nc.uap.lfw.core.event.conf.EventConf;
import nc.uap.lfw.core.exception.LfwRuntimeException;
import nc.uap.lfw.core.log.LfwLogger;
import nc.uap.lfw.core.model.PageModel;
import nc.uap.lfw.core.page.LfwView;
import nc.uap.lfw.file.FileManager;
import nc.uap.lfw.file.LfwFileConstants;
import nc.uap.lfw.jsp.uimeta.UIMeta;
import nc.uap.lfw.jsp.uimeta.UITabComp;
import nc.uap.wfm.constant.WfmConstants;
import nc.uap.wfm.exception.WfmServiceException;
import nc.uap.wfm.itf.IWfmTaskQry;
import nc.uap.wfm.model.HumAct;
import nc.uap.wfm.model.ProDef;
import nc.uap.wfm.model.Task;
import nc.uap.wfm.utils.WfmTaskUtil;
import nc.util.oa.oaod.OAODUtil;
import nc.vo.bd.defdoc.DefdocVO;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.oa.oaco.basecomp.associateform.AssociateFormVO;
import nc.vo.oa.oaod.commonenum.OfficialDocEnum;
import nc.vo.oa.oaod.officialdoctype.OfficialDocTypeVO;
import nc.vo.oa.oaod.senddoc.AggSendDocVO;
import nc.vo.oa.oaod.senddoc.SDBusinessAuthVO;
import nc.vo.oa.oaod.senddoc.SendDocVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;

import org.apache.commons.lang.StringUtils;

import uap.web.bd.pub.appenv.BDAppEnvirementHelper;

public class SendDocPageModel extends PageModel {
	
	public SendDocPageModel() {
		super();
	} 

	@SuppressWarnings("deprecation")
	@Override
	protected void initPageMetaStruct() {
		//注册页签切换事件
		regTabItemChangeEvent();
		// 把正文页签设置为不激活
		OAODUtil.addAppAttr("rendonly", "false");
		OAODUtil.setTableItemActiveStatus(this.getUIMeta(), "worditem", 0);
		String bs = OAODUtil.getAppAttr("bs") == null ? "" : OAODUtil
				.getAppAttr("bs").toString();
		String pk_senddoc = OAODUtil.getAppAttr("pk_senddoc") == null ? "-1"
				: OAODUtil.getAppAttr("pk_senddoc").toString();
		String pk_prodef = OAODUtil.getAppAttr("pk_prodef") == null ? "-1"
				: OAODUtil.getAppAttr("pk_prodef").toString();
		String humactid = OAODUtil.getAppAttr("humactid") == null ? "-1"
				: OAODUtil.getAppAttr("humactid").toString();
		String taskPk = OAODUtil.getAppAttr("taskPk") == null ? "-1" : OAODUtil
				.getAppAttr("taskPk").toString();
		
		LfwView mainWid = getPageMeta().getView(SendDocUtil.VIEW_MAINVIEW);
		LfwView menuWid = getPageMeta().getView(SendDocUtil.VIEW_MENUVIEW);
		MenubarComp menuBar = menuWid.getViewMenus().getMenuBar("menuMain");
		AggSendDocVO aggVO = null;
		SDBusinessAuthVO authVO = null;
		Task task = null;  
		if (mainWid == null)//找不到表单页面！
			throw new LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes()
					.getStrByID("odsenddocmgr", "SendDocPageModel-000000"));
		
		try {
			authVO = SendDocUtil.getSDBusinessAuthService().getBusiAuthVO(
				pk_prodef == null ? "-1" : pk_prodef,
				humactid == null ? "-1" : humactid);
			if (!pk_senddoc.equals("-1")) {
				aggVO = SendDocUtil.getSendDocService().getAggSendDocVO(
						String.format(" pk_senddoc ='%s'", pk_senddoc));
				if (StringUtils.isNotBlank(taskPk) && !taskPk.equals("-1")){
					task = (Task) WfmTaskUtil.getTaskByTaskPk(taskPk);
					if("CreateType_AfterAddSign".equals(task.getCreateType())){
						String parentTaskPk = task.getPk_parent();
						task = (Task) WfmTaskUtil.getTaskByTaskPk(parentTaskPk);
						humactid =  task.getHumActIns().getHumAct().getId();
						pk_prodef = task.getProDef().getPk_prodef();
						authVO = SendDocUtil.getSDBusinessAuthService().getBusiAuthVO(pk_prodef,humactid);
					}
				}
			}
			// authvo放入app 打印使用
			OAODUtil.addAppAttr(SendDocUtil.CONTEXT_SENDDOCBUSINESSAUTH, authVO);
			if(bs!=null&&"browse".equals(bs)){
				authVO = (SDBusinessAuthVO) OAODUtil.getAppAttr(SendDocUtil.CONTEXT_SENDDOCBUSINESSAUTH);
				OAODUtil.addAppAttr(SendDocUtil.CONTEXT_SENDDOCBUSINESSAUTH, authVO);
			}
		} catch (BusinessException e) {
		}
		if (pk_senddoc == null || pk_senddoc.equals("-1")) {
			setTempWordPk(bs);			
			// 初始化业务授权
			initSDBusinessAuth(mainWid, null, authVO, task, menuBar, bs);
		} else {
			if (aggVO != null) {
				SendDocVO sendDocVO = (SendDocVO) aggVO.getParentVO();
				OAODUtil.addAppAttr("pk_group", sendDocVO.getPk_group());
				OAODUtil.addAppAttr("pk_org", sendDocVO.getPk_org());
				// 初始化业务授权
				initSDBusinessAuth(mainWid, sendDocVO, authVO, task, menuBar,bs);
			} else {
				// 发文单不存在
				AppInteractionUtil.showMessageDialog(NCLangRes4VoTransl
						.getNCLangRes().getStrByID("odsenddocmgr","SendDocPageModel-000001"));
				AppLifeCycleContext.current().getApplicationContext()
						.closeWinDialog();				
			}			
		}
        // 设置始终不可用字段
		setFormCompUnEnable(mainWid);
		initComboxItem();
	}
	
	private void initComboxItem() {
		//add combox items and get itmes code for Business Etag 
		String wordCmbItemsHashCode =getSDWordDataCmbItems();
		OAODUtil.addAppAttr("wordCmbItemsHashCode", wordCmbItemsHashCode);
		String sdTypeCmbItemsHashCode=getSDTypeCmbItems();
		OAODUtil.addAppAttr("sdTypeCmbItemsHashCode", sdTypeCmbItemsHashCode);
		String defdocFlag = createAllDefdocCombItems();
		OAODUtil.addAppAttr("defdocFlag", defdocFlag);
	}
	
	/**
	 * 一次性获取所有自定义基础档案（静态手动加载）
	 */
	public String createAllDefdocCombItems(){
		List<Map<String,List<DefdocVO>>> listMaps = OAODUtil.getAllDefdocVOsMap();
		if(listMaps.size()<1){
			return UUID.randomUUID().toString();
		}
		Map<String, List<DefdocVO>> rel1 = listMaps.get(0);
		Map<String, List<DefdocVO>> rel2 = listMaps.get(1);
		Map<String, List<DefdocVO>> rel3 = listMaps.get(2);
		Map<String, List<DefdocVO>> rel4 = listMaps.get(3);
		Map<String, List<DefdocVO>> rel5 = listMaps.get(4);
		
		DefdocVO[] defdocVOs_ExprieDate  = rel1.get("OAOD001").toArray(new DefdocVO[0]);
		ComboData comboStatus_ExprieDate = this.getPageMeta().getView("mainView").getViewModels().getComboData("cmbd_savetime");
		CombItem[] items_ExprieDate =OAODUtil.getCombItemsByDefdocs(defdocVOs_ExprieDate);
		String flag_ExprieDate =addAllCombItems(items_ExprieDate,comboStatus_ExprieDate);
		
		DefdocVO[] defdocVOs_UrgentDegree  = rel2.get("OAOD002").toArray(new DefdocVO[0]);
		ComboData comboStatus_UrgentDegree = this.getPageMeta().getView("mainView").getViewModels().getComboData("cmbd_emergency");
		CombItem[] items_UrgentDegree =OAODUtil.getCombItemsByDefdocs(defdocVOs_UrgentDegree);
		String flag_UrgentDegree =addAllCombItems(items_UrgentDegree,comboStatus_UrgentDegree);
		
		DefdocVO[] defdocVOs_SecurityLevel  = rel3.get("OAOD003").toArray(new DefdocVO[0]);
		ComboData comboStatus_SecurityLevel = this.getPageMeta().getView("mainView").getViewModels().getComboData("cmbd_filelevel");
		CombItem[] items_SecurityLevel =OAODUtil.getCombItemsByDefdocs(defdocVOs_SecurityLevel);
		String flag_SecurityLevel =addAllCombItems(items_SecurityLevel,comboStatus_SecurityLevel);
		
		DefdocVO[] defdocVOs_SDFileType  = rel4.get("OAOD004").toArray(new DefdocVO[0]);
		ComboData comboStatus_SDFileType= this.getPageMeta().getView("mainView").getViewModels().getComboData("cmbd_filetype");
		CombItem[] items_SDFileType =OAODUtil.getCombItemsByDefdocs(defdocVOs_SDFileType);
		String flag_SDFileType =addAllCombItems(items_SDFileType,comboStatus_SDFileType);
		
		DefdocVO[] defdocVOs_Direction  = rel5.get("OAOD005").toArray(new DefdocVO[0]);
		ComboData comboStatus_Direction = this.getPageMeta().getView("mainView").getViewModels().getComboData("cmbd_writedirect");
		CombItem[] items_Direction =OAODUtil.getCombItemsByDefdocs(defdocVOs_Direction);
		String flag_Direction =addAllCombItems(items_Direction,comboStatus_Direction);
		
		String flag = flag_ExprieDate+flag_UrgentDegree+flag_SecurityLevel+flag_SDFileType+flag_Direction;
		return flag;
	}
	public String addAllCombItems(CombItem[] items,ComboData comboStatus) {
		String tagFlag = "";
		if(items!=null){
			StringBuilder f =new StringBuilder();
			for(CombItem ci : items){
				comboStatus.addCombItem(ci);
				f.append(ci.getValue()).append(":").append(ci.getText()).append(";");
			}
			
			tagFlag = String.valueOf(f.toString().hashCode());
		}
		return tagFlag;
	}
	
	
	private String getSDTypeCmbItems()	{
		ComboData comboStatus = this.getPageMeta().getView("mainView").getViewModels().getComboData("cmbd_sdtype");
		if(null==comboStatus)
			return "";
		CombItem[] items =SendDocUtil.getAllSDTypeCombItems();
		return addAllCombItems(items, comboStatus);	
	}
	
	
	private String getSDWordDataCmbItems()
	{
		ComboData comboStatus = this.getPageMeta().getView("mainView").getViewModels().getComboData("combd_sdword");
		if(null==comboStatus)
			return "";
		String pk_group = OAODUtil.getAppAttr("pk_group") == null ? "-1" : OAODUtil.getAppAttr("pk_group").toString();
		String pk_org = OAODUtil.getAppAttr("pk_org") == null ? "-1" : OAODUtil.getAppAttr("pk_org").toString();
		String bs = (String) OAODUtil.getAppAttr("bs");
		CombItem[] items=null;
		if(bs!=null&&bs.equalsIgnoreCase(SendDocUtil.BS_BROWSE))
			items =SendDocUtil.getSDWordByPKGroup(pk_group,"send");
		else	
			items =SendDocUtil.getSDWordByCurrUser(pk_group,pk_org,"send");
		
		return addAllCombItems(items, comboStatus);	
	}
	
	
	private void regTabItemChangeEvent() {
		EventConf tabEvent = TabEvent.getAfterActivedTabItemChange();
		tabEvent.setOnserver(true);
		tabEvent.setMethodName("afterTabItemChange");
		tabEvent.setControllerClazz("nc.bs.oa.oaod.senddoc.SendDocViewController");
		UIMeta ui = (UIMeta)getUIMeta();
		UITabComp tabLayout = (UITabComp) ui.findChildById("tag0052");
		tabLayout.addEventConf(tabEvent);
		OAODUtil.addAppAttr("loaded", "false");		
		super.initPageMetaStruct();
	}
	
	/**
	 * 初始化数据
	 */
	private void initData() {
		try {
			//依文起文带入的关联表单
			String reg_sessionId = LfwRuntimeEnvironment.getWebContext().getParameter("sessionId");
			AssociateFormVO[] associateForms = AssociateFormUtil.getSessionById(reg_sessionId);
			OAODUtil.addAppAttr("relatives", associateForms);
			// 集团
			String pk_group = LfwRuntimeEnvironment.getWebContext().getParameter(
					"pk_group");
			OAODUtil.addAppAttr("pk_group", pk_group);
			// 组织
			String pk_org = LfwRuntimeEnvironment.getWebContext().getParameter(
					"pk_org");
			OAODUtil.addAppAttr("pk_org", pk_org);
			BDAppEnvirementHelper.setPk_org(pk_org);
			// 发文单pk
			String pk_senddoc = LfwRuntimeEnvironment.getWebContext().getParameter(
					"pk_senddoc");
			OAODUtil.addAppAttr("pk_senddoc", pk_senddoc);
			// 发文类型
			String pk_type = LfwRuntimeEnvironment.getWebContext().getParameter(
					"pk_type");
			OAODUtil.addAppAttr("pk_type", pk_type);
			// 任务类型
			String taskPk = LfwRuntimeEnvironment.getWebContext().getParameter(
					WfmConstants.WfmUrlConst_TaskPk);
			OAODUtil.addAppAttr(WfmConstants.WfmAppAttr_TaskPk, taskPk);
			OAODUtil.addAppAttr(WfmConstants.WfmUrlConst_TaskPk, taskPk);
			// 发文流程类型
			String pk_prodef = LfwRuntimeEnvironment.getWebContext().getParameter(
					"pk_prodef");
			OAODUtil.addAppAttr("pk_prodef", pk_prodef);
			// 当前用户所在人工活动
			String humactid = LfwRuntimeEnvironment.getWebContext().getParameter(
					"humactid");
			OAODUtil.addAppAttr("humactid", humactid);
			// 业务状态
			String bs = LfwRuntimeEnvironment.getWebContext().getParameter("bs");
			OAODUtil.addAppAttr("bs", bs);
			// 是否代办代开
			String isdaffair = LfwRuntimeEnvironment.getWebContext().getParameter(
					"isdaffair");
			OAODUtil.addAppAttr("isdaffair", isdaffair);
			OAODUtil.addAppAttr(LfwFileConstants.SYSID, "bafile");
			OAODUtil.addAppAttr(LfwFileConstants.Par_Bamodule, "oaod");

			// if 发文拟稿时
			// 1.哪么pk_senddoc=-1 或者操作状态是草稿，哪么
			// 2.不显示发文单业务操作按钮
			// 需要把流程类型的pk放入session，便于流程提交时去取
			// else发文审批时
			// 1.需要把任务pk放入session
			OfficialDocTypeVO typevo = null;
			IOfficialDocTypeService odtypeservice = NCLocator.getInstance().lookup(
					IOfficialDocTypeService.class);
			if (pk_senddoc != null && pk_senddoc.equals("-1")) {
				// 清除缓存
				OAODUtil.addAppAttr(WfmConstants.WfmAppAttr_BillID, "");
				OAODUtil.addAppAttr(WfmConstants.WfmAppAttr_TaskPk, "");
				try {
					typevo = SendDocUtil.getOfficialDocTypeService().queryByKeyLfw(
							pk_type);
					OAODUtil.addAppAttr("senddoc_typevo",typevo);
					if (typevo != null) {
						OAODUtil.addAppAttr(WfmConstants.WfmAppAttr_FolwTypePk,
								typevo.getDispatchflwtype());
						// yangzw 新增属性
						OAODUtil.addAppAttr(SendDocUtil.CONTEXT_TEMPLATEPKTAG, typevo.getRegtemplate());
						OAODUtil.addAppAttr(SendDocUtil.CONTEXT_TEMPLATETSTAG, typevo.getTs().toString());
						// 重新获取pk_prodef，humAct
						if (bs != null
								&& !bs.equalsIgnoreCase(SendDocUtil.BS_BROWSE)) {
							// 获取当前用户有权限的流程定义
							ProDef prodef = odtypeservice.getCurrentUserProdef(
									typevo, OfficialDocEnum.SENDDOC, pk_org);
							if (prodef != null) {
								pk_prodef = prodef.getPk_prodef();
								HumAct humAct = prodef.getStartHumanAct();
								if (humAct != null)
									humactid = prodef.getStartHumanAct().getId();

								OAODUtil.addAppAttr("pk_prodef", pk_prodef);
								OAODUtil.addAppAttr("humactid", humactid);
							}
						}
					}
				} catch (BusinessException e) {
				}
			} else {
				// 得到senddocvo
				SendDocVO vo = null;
				try {
					vo = SendDocUtil.getSendDocVO(pk_senddoc);
					if (vo != null) {
						OAODUtil.addAppAttr("selectedOrg", vo.getSdorg());//审批时初始的发文组织作为发文部门参照的条件
						pk_type = vo.getSdtype();
						typevo = SendDocUtil.getOfficialDocTypeService()
								.queryByKey(pk_type);
						if (typevo != null)
							OAODUtil.addAppAttr(WfmConstants.WfmAppAttr_FolwTypePk,
									typevo.getDispatchflwtype());
					}
				} catch (BusinessException e1) {
					LfwLogger.error(e1);
				}
				String fromAssociateLink = LfwRuntimeEnvironment.getWebContext().getParameter("fromAssociateLink");
				if(fromAssociateLink!=null){
					OAODUtil.addAppAttr(
							WfmConstants.WfmAppAttr_TaskPk,
							LfwRuntimeEnvironment.getWebContext().getParameter("pk_taskFromass"));
					// 附件按钮权限控制
					OAODUtil.addAppAttr(DefaultFileMgrView.FILEMMGRVIEWCLAZZES, "nc.impl.oa.oaod.filemgr.BillFileMgrViewImpl"); 
				
				}else{
					OAODUtil.addAppAttr(WfmConstants.WfmAppAttr_BillID,
							vo.getPk_senddoc());
					OAODUtil.addAppAttr(WfmConstants.WfmAppAttr_Status,
							vo.getDocstate());
					OAODUtil.addAppAttr(
							WfmConstants.WfmAppAttr_TaskPk,
							LfwRuntimeEnvironment.getWebContext().getParameter(
									WfmConstants.WfmUrlConst_TaskPk));		
				}
				// 设置附件到session，便于流程附件用到
				OAODUtil.addAppAttr(
						WfmConstants.WfmAppAttr_FormInFoCtx_Billitem,
						vo.getFileitem());
			}
		} catch (nc.md.model.MetaDataRuntimeException  e) {
			 Logger.error("测试发文拟稿偶尔出错原因....");
		}
	}

	private void setTempWordPk(String bs) {
		String wordPk = null;
		try {
			FileManager fileMgr = OAODUtil.getSystemFileManagerByBafile();
			if (bs != null) {
				if (bs.equals("newbyreg")) {
				 SendDocByRegData sendDocByRegData = (SendDocByRegData) OAODUtil.getSessionCacheAttr(SendDocByRegData.SESSIONCONTEXTKEY);
				 String newFile = OAODUtil.copyFile(sendDocByRegData.getPk_word());
				 wordPk = newFile;
			    }else{
			    	InputStream ins = null;
			    	try{
		    			ins = fileMgr.getEmptyWordStream(); 
		    			wordPk = fileMgr.upload("send.docx", null, "", 0, ins);
			    	}
			    	finally{
			    		if(ins != null)ins.close();
			    	}
			    }
			}else{
				 wordPk = fileMgr.upload("send.docx", null, "", 0, null);
			}
			OAODUtil.addAppAttr("wordPK", wordPk);
		} catch (Exception e) {
			throw new LfwRuntimeException(e);
		}
	}	

	/**
	 * 根据单据的状态，以及当然流程节点操作人的权限，来设置控件的显示 规则如下：
	 * 1.单据是开立状态：用户发文拟稿，或用户暂存后在打开，哪么控件均显示暂存和提交 2.如果用户通过我的发文来查看处理中的发文，哪么发文单应该置灰
	 * 3.其他单据状态，哪么应该单据状态以及当前登陆用户的权限显示控件
	 * 
	 * @param mainWid
	 * @param sendDocVO
	 * @param menuBar
	 * @param bs
	 */
	private void initSDBusinessAuth(LfwView mainWid, SendDocVO sendDocVO,
			SDBusinessAuthVO authVO, Task task, MenubarComp menuBar, String bs) {		
		FormComp frmSendDoc = (FormComp) mainWid.getViewComponents()
				.getComponent(SendDocUtil.MAINFORM);		
		String taskPk = OAODUtil.getAppAttr("taskPk") == null ? "-1" : OAODUtil
				.getAppAttr("taskPk").toString();	

		// 如果单据是开立，根据授权进行显示
		if (sendDocVO == null
				|| (sendDocVO.getDocstate() != null && sendDocVO.getDocstate()
						.equals("NottStart"))) {
			if (bs != null && bs.equalsIgnoreCase(SendDocUtil.BS_BROWSE)) {				
				setMenuVisiable(menuBar, null, bs, taskPk, task,null);
			} else {				
				setMenuVisiable(menuBar, authVO, bs, taskPk, task,null);
				setWidgetEnableByBusiness(mainWid, sendDocVO, menuBar, bs,
						taskPk, task,authVO);
			}
		} else {
			// 如果单据不是开立状态，则提交隐藏，其他根据当前登陆用户决定是否显示
			// 如果用户通过我的发文来查看处理中的发文，哪么发文单应该置灰
			if (bs != null && bs.equalsIgnoreCase(SendDocUtil.BS_BROWSE)) {				
				setMenuVisiable(menuBar, getSDBusinessAuthVO(authVO), bs, taskPk, task,sendDocVO);				
			} else {
				// 如果是处理中，则根据用户权限显示控件
				if (sendDocVO.getDocstate() != null
						&& sendDocVO.getDocstate().equals(OAODUtil.WFM_RUN)) {
					
					setMenuVisiable(menuBar, authVO, bs, taskPk, task,sendDocVO);
					setFormColumnUnEnableByAuth(frmSendDoc, authVO);
				}
				setWidgetEnableByBusiness(mainWid, sendDocVO, menuBar, bs,
						taskPk, task,authVO);
			}
		}
	}

	private void setWidgetEnableByBusiness(LfwView mainWid,
			SendDocVO sendDocVO, MenubarComp menuBar, String bs, String taskPk,
			Task task ,SDBusinessAuthVO authvo) {
		
		if (taskPk != null && !taskPk.equals("-1")) {
			if (task != null) {
				// task.getPk_task()和taskPk在后加签时有区别
				if (!OAWfmUtil.judgeTheDocCanChangeByTaskpk(taskPk)) {
					OAODUtil.setDatasetEnable(mainWid.getViewModels().getDataset(SendDocUtil.DATASET_SENDDOC), false);
					setMenuVisiable(menuBar, getSDBusinessAuthVO(authvo), bs, taskPk, task,sendDocVO);
				}
			}
		}
		// 如果是作废，结束，挂起，则页面置灰
		if (sendDocVO != null
				&& sendDocVO.getDocstate() != null
				&& (sendDocVO.getDocstate().equals(OAODUtil.WFM_END)
						|| sendDocVO.getDocstate().equals(
								OAODUtil.WFM_CANCELLATION) || sendDocVO
						.getDocstate().equals(OAODUtil.WFM_SUSPENDED))) {
			setMenuVisiable(menuBar, getSDBusinessAuthVO(authvo), bs, taskPk, task,sendDocVO);
		}
	}

	/**
	 * 根据授权设置不可用的字段
	 * 
	 * @param frmSendDoc
	 * @param authVO
	 */
	private void setFormColumnUnEnableByAuth(FormComp frmSendDoc,
			SDBusinessAuthVO authVO) {
		if (frmSendDoc != null) {
//			// 编写文号未授权，不许编写发文字号
//			if (!authVO.getIseditsdword().booleanValue()) {
//				FormElement sdWord = frmSendDoc.getElementById("sdword");
//				if (sdWord != null)
//					sdWord.setEnabled(false);
//			}
			//不再控制
//			// 分发未授权，不许分发
//			if (!authVO.getIsdispense().booleanValue()) {
//				FormElement mainscope = frmSendDoc.getElementById("mainscope");
//				if (mainscope != null)
//					mainscope.setEnabled(false);
//				FormElement mainscope_name = frmSendDoc
//						.getElementById("mainscope_name");
//				if (mainscope_name != null)
//					mainscope_name.setEnabled(false);
//				FormElement annxscope = frmSendDoc.getElementById("annxscope");
//				if (annxscope != null)
//					annxscope.setEnabled(false);
//				FormElement annxscope_name = frmSendDoc
//						.getElementById("annxscope_name");
//				if (annxscope_name != null)
//					annxscope_name.setEnabled(false);
//			}
		}
	}

	/***
	 * 流程结束也可以联查交换单、打印
	 * @param authvo
	 * @return
	 */
	private SDBusinessAuthVO getSDBusinessAuthVO(SDBusinessAuthVO authvo){
		SDBusinessAuthVO vo = new SDBusinessAuthVO();
		if(null != authvo){
			vo.setIscheck(authvo.getIscheck());
			vo.setIsprint(authvo.getIsprint());
			vo.setIsopinion(authvo.getIsopinion());
			vo.setIsdocument(authvo.getIsdocument());
			vo.setIsfilesaveas(authvo.getIsfilesaveas());

			if("-1".equals(authvo.getHumactid())){//已拟浏览处理中的单据
				if(OAODUtil.getAppAttr("isPrint")!=null)
				vo.setIsprint(UFBoolean.valueOf(OAODUtil.getAppAttr("isPrint").toString()));				
				if(OAODUtil.getAppAttr("isFilesaveas")!=null)
				vo.setIsfilesaveas(UFBoolean.valueOf(OAODUtil.getAppAttr("isFilesaveas").toString()));
			}
		}else{
			vo.setIscheck(UFBoolean.valueOf(false));
			vo.setIsprint(UFBoolean.valueOf(false));
			vo.setIsdocument(UFBoolean.valueOf(false));
			vo.setIsopinion(UFBoolean.valueOf(false));
			vo.setIsfilesaveas(UFBoolean.valueOf(false));

			//查询或管理节点打开，无条件拥有打印权限
			String nodecode = (String) OAODUtil.getAppAttr(AppControlPlugin.NODECODE);
			if(nodecode.equals(OAODUtil.SENDMANAGE)||nodecode.equals(OAODUtil.SENDQuery)){
				vo.setIsprint(UFBoolean.valueOf(true));
				vo.setIsdocument(UFBoolean.valueOf(true));
				vo.setIsopinion(UFBoolean.valueOf(true));
			}
			
		}
		vo.setIschromdoc(UFBoolean.valueOf(false));
		vo.setIsdispense(UFBoolean.valueOf(false));
		vo.setIsdocmented(UFBoolean.valueOf(false));
		vo.setIseditchromdoc(UFBoolean.valueOf(false));
		vo.setIseditsdword(UFBoolean.valueOf(false));
		vo.setIssdedittext(UFBoolean.valueOf(false));
		vo.setIssdend(UFBoolean.valueOf(false));
		vo.setIssign(UFBoolean.valueOf(false));
		vo.setIssign_send(UFBoolean.valueOf(false));
		vo.setIsundocmented(UFBoolean.valueOf(false));
		vo.setIsunsdend(UFBoolean.valueOf(false));
		return vo;
	}
	
	/**
	 * 设置菜单的可见性
	 * 
	 * @param menuBar
	 * @param vo
	 */
	private void setMenuVisiable(MenubarComp menuBar, SDBusinessAuthVO vo,
			String bs, String taskPk, Task task ,  SendDocVO senddocvo) {
		List<String> list = new ArrayList<String>();
		// 编写文号
		if (vo == null || !vo.getIseditsdword().booleanValue())
			list.add("EditSDWord");
		// 正文模板
		if (vo == null || !vo.getIssdedittext().booleanValue()){
			list.add("SDTextTemplet");
			//单据设置有用印、套红、(编辑红头权限并且单据已经套红，可以编辑正文 模板
			if( null != vo){
				if(vo.getIssign().booleanValue()||vo.getIschromdoc().booleanValue()||
						(vo.getIseditchromdoc().booleanValue()&& (senddocvo == null ? false : senddocvo.getIschrom().booleanValue()))){
					list.remove("SDTextTemplet");
				}
			}
		}
		// 套红
		if (vo == null || !vo.getIschromdoc().booleanValue())
			list.add("ChromDoc");
		// 用印
		if (vo == null || !vo.getIssign().booleanValue())
			list.add("Sign");
		// 分发
		if (vo == null || !vo.getIsdispense().booleanValue())
			list.add("Dispense");
		// 注结
		if (vo == null || !vo.getIssdend().booleanValue())
			list.add("SDEnd");
		// 撤销注结
		if (vo == null || !vo.getIsunsdend().booleanValue())
			list.add("UnSDEnd");
		// 归档
		if (vo == null || !vo.getIsdocmented().booleanValue())
			list.add("Docmented");
		// 撤销归档
		if (vo == null || !vo.getIsundocmented().booleanValue())
			list.add("UnDocmented");
		//打印
		if (vo == null || !vo.getIsprint().booleanValue())
			list.add("Print");
		// 单据审批完成 作废 也可以打印
		if (vo != null && !vo.getIsprint().booleanValue()){
			list.add("Print");
		}
		String nodecode = (String) OAODUtil.getAppAttr(AppControlPlugin.NODECODE); // 从归档节点打开 有打印权限并且已注结
		if(nodecode.equals(OAODUtil.SENDNODE)&&(vo != null && vo.getIsprint().booleanValue())&&(null!=senddocvo)&&(senddocvo.getIsend().booleanValue())){
			list.add("Print");
		}
		//从归档节点打开没有 有打印权限
		if(nodecode.equals(OAODUtil.SENDNODE)&&(vo != null && !vo.getIsprint().booleanValue()))
			list.add("Print");
//		// 联查交换单,受单据权限控制，且归档节点打开 E3301601无 权限
		if(((vo != null && !vo.getIscheck().booleanValue()) || null == vo) || (vo != null && nodecode.equals(OAODUtil.SENDNODE))  ){
			list.add("Check");
		}
		// 浏览记录
		if((vo != null && !vo.getIsskim().booleanValue()) || null == vo ){
			list.add("Skim");
		}
		// 浏览记录     管理节点打开 E3240122
		if(vo != null && nodecode.equals(OAODUtil.SENDMANAGE)){
			list.add("Skim");
		}
		// 浏览记录  归档节点打开 E3301601
		if(vo != null && nodecode.equals(OAODUtil.SENDNODE)){
			list.add("Skim");
		} 
		// 删除
		if (bs == null
				|| !bs.equals(SendDocUtil.BS_DRAFT)
				|| (bs.equals(SendDocUtil.BS_DRAFT) && task != null && task
						.getState().equalsIgnoreCase(Task.State_Suspended)))
			list.add("delete");

		if (list.size() > 0){
			//暂时删除浏览记录按钮
			list.add("Skim");
			OAODUtil.setRemoveMenuItem(menuBar, list);
		}
		StringBuilder sb = new StringBuilder(); 
		for(MenuItem item : menuBar.getMenuList())
			sb.append(item.getId());
		OAODUtil.addAppAttr(SendDocUtil.CONTEXT_MENUTAG,sb.toString() );	
	}

	/**
	 * 设置始终不能编辑列，例如单据状态、发文文号、发文类型
	 * 
	 * @param mainWid
	 */
	private void setFormCompUnEnable(LfwView mainWid) {
		List<String> compList = new ArrayList<String>();
		compList.add("sdmark");
		compList.add("docstate");
		compList.add("creator");
		compList.add("sdtype");
		compList.add("signor");
		compList.add("signdate");
		compList.add("signidea");
		compList.add("billno");
		compList.add("ts");
		compList.add("fileitem");
		OAODUtil.setFormCompEnable(mainWid, compList, SendDocUtil.MAINFORM,
				false);
	}

	
	@Override
	public LinkedHashMap<String, String> getDimensions() {
		return OAODUtil.getDimensions("0001ZC1000000003MUO2", "NewSendDocApp",
				"SendDocWin", (String) OAODUtil
						.getAppAttr(WfmConstants.WfmAppAttr_FolwTypePk),
				(String) OAODUtil.getAppAttr("pk_prodef"), (String) OAODUtil
						.getAppAttr("humactid"));
	}

	@Override
	public String getTemplatePK() {
		initData();
		String templatepk = null;
		String taskPk = LfwRuntimeEnvironment.getWebContext().getParameter(WfmConstants.WfmUrlConst_TaskPk);
		IWfmTaskQry taskService = NCLocator.getInstance().lookup(IWfmTaskQry.class);
		try
		{
			Task task = taskService.getTaskByPk(taskPk);
			if (null != task)
			{
				templatepk = task.getHumActIns().getHumAct().getPk_template();
			}
		}
		catch (WfmServiceException e)
		{
			throw new LfwRuntimeException(e);
		}
		if (StringUtils.isEmpty(templatepk)) {
			try {
				OfficialDocTypeVO typevo = (OfficialDocTypeVO) OAODUtil.getAppAttr("senddoc_typevo");
				if (typevo != null)
					return typevo.getSdtemplate();
				else
				{
					String pk_senddoc = (String) OAODUtil.getAppAttr("pk_senddoc");
					if(StringUtils.isNotEmpty(pk_senddoc)){
						SendDocVO sendvo = (SendDocVO)SendDocUtil.getSendDocService().getAggSendDocVOByKey(pk_senddoc).getParentVO();
						//从发文单取得公文类型的个性化模板
						typevo = SendDocUtil
								.getOfficialDocTypeService().queryByKey(sendvo.getSdtype());
						if(typevo != null){
							return typevo.getSdtemplate();
						}
					}	
				}
			} catch (BusinessException e) {
				throw new LfwRuntimeException(e);
			}
		}
		return templatepk;
	}

	@Override
	public String getBusinessEtag() {
		StringBuilder sb = new StringBuilder();
		sb.append(OAODUtil.getAppAttr("wordCmbItemsHashCode"));		
		sb.append(OAODUtil.getAppAttr("sdTypeCmbItemsHashCode"));
		sb.append(OAODUtil.getAppAttr("defdocFlag"));
		
		//依据模版,模版最新更新时间,显示菜单来确定是否使用缓存
		Object templateTag = OAODUtil.getAppAttr(SendDocUtil.CONTEXT_TEMPLATEPKTAG);
		templateTag = String.valueOf(((null == templateTag)?"":templateTag).hashCode());				
		String templateTS = String.valueOf(OAODUtil.getAppAttr(SendDocUtil.CONTEXT_TEMPLATETSTAG));
		String menuTag = String.valueOf(OAODUtil.getAppAttr(SendDocUtil.CONTEXT_MENUTAG).hashCode());				
		return templateTag + templateTS + menuTag+sb.toString()+Math.random();//由于关联表单问题，暂时弃用命中session的优化
	}

}
