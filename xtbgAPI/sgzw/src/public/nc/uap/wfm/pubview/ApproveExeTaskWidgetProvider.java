package nc.uap.wfm.pubview;

import nc.uap.lfw.core.LfwRuntimeEnvironment;
import nc.uap.lfw.core.WebContext;
import nc.uap.lfw.core.combodata.StaticComboData;
import nc.uap.lfw.core.comp.ButtonComp;
import nc.uap.lfw.core.comp.RadioGroupComp;
import nc.uap.lfw.core.comp.TextAreaComp;
import nc.uap.lfw.core.comp.text.ComboBoxComp;
import nc.uap.lfw.core.page.LfwView;
import nc.uap.lfw.core.page.ViewComponents;
import nc.uap.lfw.core.page.ViewModels;
import nc.uap.lfw.jsp.uimeta.UIBorder;
import nc.uap.lfw.jsp.uimeta.UICardLayout;
import nc.uap.lfw.jsp.uimeta.UICardPanel;
import nc.uap.lfw.jsp.uimeta.UIConstant;
import nc.uap.lfw.jsp.uimeta.UIFlowhLayout;
import nc.uap.lfw.jsp.uimeta.UIFlowhPanel;
import nc.uap.lfw.jsp.uimeta.UIFlowvLayout;
import nc.uap.lfw.jsp.uimeta.UIFlowvPanel;
import nc.uap.lfw.jsp.uimeta.UILabelComp;
import nc.uap.lfw.jsp.uimeta.UIListViewComp;
import nc.uap.lfw.jsp.uimeta.UIMeta;
import nc.uap.lfw.jsp.uimeta.UIPanel;
import nc.uap.lfw.jsp.uimeta.UIPartComp;
import nc.uap.lfw.jsp.uimeta.UITextField;
import nc.uap.wfm.exetask.ApproveExeTaskMainCtrl;
import nc.uap.wfm.exetask.ExeTaskApproveState;
import nc.uap.wfm.exetask.ExeTaskBaseState;
import nc.uap.wfm.exetask.ExeTaskBroseState;
import nc.uap.wfm.exetask.ExeTaskReadState;
import nc.uap.wfm.utils.WfmProinsUtil;
import nc.uap.wfm.utils.WfmTaskUtil;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import org.apache.commons.lang.StringUtils;
import uap.web.bd.pub.AppUtil;

public class ApproveExeTaskWidgetProvider extends ExecuteTaskWidgetProvider
{
  protected void initWidget(LfwView widget, String taskPk, String billstate)
  {
    widget.setControllerClazz(ApproveExeTaskMainCtrl.class.getName());

    new ExeTaskApproveState().createApproveWidget(widget, taskPk);
    Object task = WfmTaskUtil.getTaskFromSessionCache(taskPk);
    if ("billstate_makebill".equals(billstate)) {
      clearOperate(widget);
    } else if ("billstate_browse".equals(billstate)) {
      clearOperate(widget);

      if (WfmTaskUtil.isCanReCall(task))
        new ExeTaskBaseState().createReCall(widget, task, false);
    }
    else if ("billstate_read".equals(billstate))
    {
      if ((WfmTaskUtil.isUnreadState(task)) || (WfmTaskUtil.isReadEndState(task)) || (WfmTaskUtil.isReadedState(task))) {
        new ExeTaskReadState().createReadEndCall(widget, task);
      }

    }
    else
    {
      RadioGroupComp rgc = (RadioGroupComp)widget.getViewComponents().getComponent("text_exeaction");
      rgc.setSepWidth(5);
    }
  }

  private StaticComboData clearOperate(LfwView widget)
  {
    StaticComboData staticComboData = (StaticComboData)widget.getViewModels().getComboData("exeaction");

    staticComboData.removeComboItem("transmit");

    staticComboData.removeComboItem("stop");

    staticComboData.removeComboItem("beforeAddSign");

    staticComboData.removeComboItem("reject");
    return staticComboData;
  }

  public UIMeta getDefaultUIMeta(LfwView widget)
  {
    String billID = LfwRuntimeEnvironment.getWebContext().getParameter("billID");
    String taskPk = LfwRuntimeEnvironment.getWebContext().getParameter("taskPk");

    if ((StringUtils.isBlank(taskPk)) && (StringUtils.isNotBlank(billID))) {
      taskPk = getTaskPk(billID);
    }
    if (taskPk != null) {
      WfmTaskUtil.addTaskPkToSession(taskPk);
    }
    Object task = WfmTaskUtil.getTaskFromSessionCache(taskPk);
    String widgetId = widget.getId();
    UIMeta um = new UIMeta();
    um.setId(widgetId + "_um");
    um.setFlowmode(Boolean.TRUE);
    um.setIncludejs("wfinclude.js");

    UIFlowhLayout gflowh = new UIFlowhLayout();
    gflowh.setAutoFill(Integer.valueOf(0));
    gflowh.setId("gflowh");
    um.setElement(gflowh);

    UIFlowhPanel leftPaddingPanel = gflowh.addElementToPanel(null);
    leftPaddingPanel.setWidth("1");

    UIBorder gborder = new UIBorder();
    gborder.setId("gborder");
    gborder.setCssStyle("background:#FCFCFC;border-style: hidden;");
    gborder.setRoundBorder(UIConstant.TRUE.intValue());
    gflowh.addElementToPanel(gborder);
    createApprovePanel(widget, gborder, task);

    UIFlowhPanel rightPaddingPanel = gflowh.addElementToPanel(null);
    rightPaddingPanel.setWidth("1");

    um.adjustUI(widgetId);

    AppUtil.addAppAttr("approveWiget", "Y");
    return um;
  }

  private void createApprovePanel(LfwView widget, UIBorder gborder, Object task)
  {
    UIFlowhLayout bgFlowh = new UIFlowhLayout();
    bgFlowh.setId("bgflowh");
    gborder.addElementToPanel(bgFlowh);

    UIFlowhPanel bgLeftPadding = bgFlowh.addElementToPanel(null);
    bgLeftPadding.setWidth("5");

    createInnerApprovePanel(widget, bgFlowh, task);

    UIFlowhPanel rightPaddingPanel = bgFlowh.addElementToPanel(null);
    rightPaddingPanel.setWidth("5");
  }

  private void createInnerApprovePanel(LfwView widget, UIFlowhLayout bgFlowh, Object task)
  {
    UIPanel approvePanel = new UIPanel();
    approvePanel.setId("panel1");
    approvePanel.setTitle(NCLangRes4VoTransl.getNCLangRes().getStrByID("wfm", "ExecuteTaskWidgetProvider-000020"));
    bgFlowh.addElementToPanel(approvePanel);
    approvePanel.setRenderType(null);
    approvePanel.setExpand(UIConstant.TRUE);

    UIFlowvLayout contentFlowh = new UIFlowvLayout();
    contentFlowh.setId("contentFlowh");
    approvePanel.addElementToPanel(contentFlowh);

    createApproveByState(widget, contentFlowh, task);

    setWfmHistory(widget, contentFlowh);
  }

  private void createApproveByState(LfwView widget, UIFlowvLayout contentFlowh, Object task)
  {
    UIFlowvLayout flowv = new UIFlowvLayout();
    flowv.setId("flowvlayout0191");
    UIFlowvPanel contentFlowhP = contentFlowh.addElementToPanel(flowv);
    contentFlowhP.setId("contentFlowhP");

    String billstate = (String)AppUtil.getAppAttr("$$$$$$billstate");

    if ("billstate_makebill".equals(billstate)) {
      createMakeBillPage(widget, flowv);
    }
    else if ("billstate_browse".equals(billstate)) {
      createBrowsePage(widget, flowv, task);
    }
    else if ("billstate_read".equals(billstate)) {
      createReadPage(widget, flowv);
    }
    else if ("billstate_readonly".equals(billstate)) {
      createReadOnlyPage(widget, flowv);
    }
    else
    {
      createApprovePage(widget, flowv);
    }
  }

  private void createApprovePage(LfwView widget, UIFlowvLayout flowv)
  {
    createOperator(flowv);

    createOpinionAndComLan(widget, flowv, true);

    UICardLayout card1 = new UICardLayout();
    card1.setId("card1");
    card1.setCurrentItem("0");
    flowv.addElementToPanel(card1);
    UICardPanel cp1 = new UICardPanel();
    cp1.setId("cardpanel1");
    card1.addPanel(cp1);

    UICardPanel cp2 = new UICardPanel();
    cp2.setId("cardpanel2");
    card1.addPanel(cp2);
    UIFlowvLayout cp2flowv = new UIFlowvLayout();
    cp2flowv.setId("flowvlayout1683");
    cp2.setElement(cp2flowv);
    UITextField transuser = new UITextField();
    transuser.setId("text_transmituser");
    transuser.setWidth("250");
    UIFlowvPanel cp2flowvPanel = cp2flowv.addElementToPanel(transuser);
    cp2flowvPanel.setHeight("30");

    UICardPanel cp3 = new UICardPanel();
    cp3.setId("cardpanel3");
    card1.addPanel(cp3);
    UIFlowvLayout cp3flowv = new UIFlowvLayout();
    cp3flowv.setId("flowvlayout1783");
    cp3.setElement(cp3flowv);

    UIFlowhLayout addsignFlowh = new UIFlowhLayout();
    addsignFlowh.setId("addsignFlowh");
    UIFlowvPanel cp3flowvP1 = cp3flowv.addElementToPanel(addsignFlowh);
    cp3flowvP1.setHeight("50");

    UITextField addsignUser = new UITextField();
    addsignUser.setId("text_beforeaddsignuser");
    UIFlowhPanel addsignUserPanel = addsignFlowh.addElementToPanel(addsignUser);

    UITextField logic = new UITextField();
    logic.setId("text_logic");
    logic.setWidth("180");
    UIFlowhPanel logicPanel = addsignFlowh.addElementToPanel(logic);

    UIFlowhLayout flowh = new UIFlowhLayout();
    flowh.setId("flowhlayout5101");
    flowv.addElementToPanel(flowh);

    if (isVisible(widget, "link_copysend")) {
      new ExeTaskBaseState().createUILinkComp(widget, flowh, "link_copysend", "40", null, null, null, null, null);
    }

    if (isVisible(widget, "text_scratchpad")) {
      new ExeTaskBaseState().createUILinkComp(widget, flowh, "text_scratchpad", "40", null, null, null, null, null);
    }

    if (isVisible(widget, "link_aftaddsign")) {
      new ExeTaskBaseState().createUILinkComp(widget, flowh, "link_aftaddsign", "50", null, null, null, null, null);
    }

    if (isVisible(widget, "link_addsignmgr")) {
      new ExeTaskBaseState().createUILinkComp(widget, flowh, "link_addsignmgr", "60", null, null, null, null, null);
    }

    if (isVisible(widget, "link_flowimg")) {
      new ExeTaskBaseState().createUILinkComp(widget, flowh, "link_flowimg", "60", null, null, null, null, null);
    }

    if (isVisible(widget, "link_urgencyhistory")) {
      new ExeTaskBaseState().createUILinkComp(widget, flowh, "link_urgencyhistory", "60", null, null, null, null, null);
    }

    if (isVisible(widget, "link_stepopinion")) {
      new ExeTaskBaseState().createUILinkComp(widget, flowh, "link_stepopinion", "60", null, null, null, null, null);
    }

    if (isVisible(widget, "link_addattach")) {
      new ExeTaskBaseState().createUIAttachAndImageComp(widget, flowh, "60", null, null, null, null, null);
    }

    UIFlowvLayout dynFlowv = new UIFlowvLayout();
    dynFlowv.setId("dynflowv");
    flowv.addElementToPanel(dynFlowv);

    UIFlowvPanel dynp1 = new UIFlowvPanel();
    dynp1.setId("dynpanel1");
    dynFlowv.addPanel(dynp1);
    UIFlowhLayout layout_copySend = new UIFlowhLayout();
    layout_copySend.setId("layout_copysend");
    dynp1.setElement(layout_copySend);

    UILabelComp csTextL = new UILabelComp();
    csTextL.setId("label_copysend");
    csTextL.setWidth(null);
    UIFlowhPanel copySend = layout_copySend.addElementToPanel(csTextL);
    copySend.setWidth("50");

    UILabelComp csTextName = new UILabelComp();
    csTextName.setWidth(null);
    csTextName.setId("name_copysend");
    csTextName.setTextAlign("left");
    layout_copySend.addElementToPanel(csTextName);

    UILabelComp csText = new UILabelComp();
    csText.setId("value_copysend");
    layout_copySend.addElementToPanel(csText);

    UIFlowvPanel dynp3 = new UIFlowvPanel();
    dynp3.setId("dynpanel3");
    dynp3.setVisible(false);
    dynFlowv.addPanel(dynp3);

    UIFlowhLayout layout_afteraddsign = new UIFlowhLayout();
    layout_afteraddsign.setId("layout_afteraddsign");
    dynp3.setElement(layout_afteraddsign);

    UILabelComp afterSignText = new UILabelComp();
    afterSignText.setWidth(null);
    afterSignText.setId("label_afteraddsign");
    UIFlowhPanel afterSign = layout_afteraddsign.addElementToPanel(afterSignText);
    afterSign.setWidth("80");

    UILabelComp afterSignName = new UILabelComp();
    afterSignName.setWidth(null);
    afterSignName.setId("name_afteraddsign");
    layout_afteraddsign.addElementToPanel(afterSignName);

    UILabelComp affterSignValue = new UILabelComp();
    affterSignValue.setId("value_afteraddsign");
    affterSignValue.setVisible(false);
    layout_afteraddsign.addElementToPanel(affterSignValue);

    UIFlowhLayout bottomFlowh = new UIFlowhLayout();
    bottomFlowh.setId("bottomFlowh");
    flowv.addElementToPanel(bottomFlowh);

    new ExeTaskBroseState().createUIButton(widget, bottomFlowh, "btn_ok", "64", "right", null, "0", null, "0", "blue_button_div");

    ButtonComp stepButton = (ButtonComp)widget.getViewComponents().getComponent("btn_step_ok");
    if (stepButton != null) {
      new ExeTaskBroseState().createUIButton(widget, bottomFlowh, "btn_step_ok", "64", "right", null, "0", null, "0", null);
    }

    new ExeTaskBroseState().createUIButton(widget, bottomFlowh, "btn_save", "64", "right", null, "0", null, "0", null);
  }

  private void createReadOnlyPage(LfwView widget, UIFlowvLayout flowv)
  {
    createOpinionAndComLan(widget, flowv, false);

    UIFlowhLayout flowh = new UIFlowhLayout();
    flowh.setId("flowhlayout5101");
    flowv.addElementToPanel(flowh);
    UIFlowhLayout bottomFlowh = new UIFlowhLayout();
    bottomFlowh.setId("bottomFlowh");
    flowv.addElementToPanel(bottomFlowh);

    if (isVisible(widget, "link_flowimg")) {
      new ExeTaskBaseState().createUILinkComp(widget, flowh, "link_flowimg", "60", null, null, null, null, null);
    }

    if (isVisible(widget, "link_addattach")) {
      new ExeTaskBaseState().createUIAttachAndImageComp(widget, flowh, "60", null, null, null, null, null);
    }
    if (isVisible(widget, "link_supopinion"))
      new ExeTaskBaseState().createUILinkComp(widget, flowh, "link_supopinion", "60", null, null, null, null, null);
  }

  private void createReadPage(LfwView widget, UIFlowvLayout flowv)
  {
    createOperator(flowv);

    createOpinionAndComLan(widget, flowv, false);

    UIFlowhLayout flowh = new UIFlowhLayout();
    flowh.setId("flowhlayout5101");
    flowv.addElementToPanel(flowh);

    if (isVisible(widget, "link_copysend")) {
      new ExeTaskBaseState().createUILinkComp(widget, flowh, "link_copysend", "40", null, null, null, null, null);
    }

    UIFlowvLayout dynFlowv = new UIFlowvLayout();
    dynFlowv.setId("dynflowv");
    flowv.addElementToPanel(dynFlowv);

    UIFlowvPanel dynp1 = new UIFlowvPanel();
    dynp1.setId("dynpanel1");
    dynFlowv.addPanel(dynp1);
    UIFlowhLayout layout_copySend = new UIFlowhLayout();
    layout_copySend.setId("layout_copysend");
    dynp1.setElement(layout_copySend);

    UILabelComp csTextL = new UILabelComp();
    csTextL.setId("label_copysend");
    csTextL.setWidth(null);
    UIFlowhPanel copySend = layout_copySend.addElementToPanel(csTextL);
    copySend.setWidth("50");

    UILabelComp csTextName = new UILabelComp();
    csTextName.setWidth(null);
    csTextName.setId("name_copysend");
    csTextName.setTextAlign("left");
    layout_copySend.addElementToPanel(csTextName);

    UILabelComp csText = new UILabelComp();
    csText.setId("value_copysend");
    layout_copySend.addElementToPanel(csText);

    UIFlowhLayout bottomFlowh = new UIFlowhLayout();
    bottomFlowh.setId("bottomFlowh");
    flowv.addElementToPanel(bottomFlowh);

    if (isVisible(widget, "link_flowimg")) {
      new ExeTaskBaseState().createUILinkComp(widget, flowh, "link_flowimg", "60", null, null, null, null, null);
    }

    if (isVisible(widget, "link_addattach")) {
      new ExeTaskBaseState().createUIAttachAndImageComp(widget, flowh, "60", null, null, null, null, null);
    }
    if (isVisible(widget, "link_supopinion")) {
      new ExeTaskBaseState().createUILinkComp(widget, flowh, "link_supopinion", "60", null, null, null, null, null);
    }

    ButtonComp readEnd = (ButtonComp)widget.getViewComponents().getComponent("readend");
    if (readEnd != null)
      new ExeTaskBroseState().createUIButton(widget, bottomFlowh, "readend", "64", "right", null, "0", null, "0", null);
  }

  private void createMakeBillPage(LfwView widget, UIFlowvLayout flowv)
  {
    createOperator(flowv);

    createOpinionAndComLan(widget, flowv, false);

    UIFlowhLayout flowh = new UIFlowhLayout();
    flowh.setId("flowhlayout5101");
    flowv.addElementToPanel(flowh);

    if (isVisible(widget, "text_scratchpad")) {
      new ExeTaskBaseState().createUILinkComp(widget, flowh, "text_scratchpad", "40", null, null, null, null, null);
    }

    if (isVisible(widget, "link_flowimg")) {
      new ExeTaskBaseState().createUILinkComp(widget, flowh, "link_flowimg", "60", null, null, null, null, null);
    }

    if (isVisible(widget, "link_addattach")) {
      new ExeTaskBaseState().createUIAttachAndImageComp(widget, flowh, "60", null, null, null, null, null);
    }

    UIFlowhLayout bottomFlowh = new UIFlowhLayout();
    bottomFlowh.setId("bottomFlowh");
    flowv.addElementToPanel(bottomFlowh);

    new ExeTaskBroseState().createUIButton(widget, bottomFlowh, "btn_ok", "64", "right", null, "0", null, "0", "blue_button_div");

    new ExeTaskBroseState().createUIButton(widget, bottomFlowh, "btn_save", "64", "right", null, "0", null, "0", null);
  }

  private void createBrowsePage(LfwView widget, UIFlowvLayout flowv, Object task)
  {
    createOperator(flowv);

    createOpinionAndComLan(widget, flowv, false);

    UIFlowhLayout flowh = new UIFlowhLayout();
    flowh.setId("flowhlayout5101");
    flowv.addElementToPanel(flowh);
    if ((WfmTaskUtil.allowSupOperate(task)) && (!(WfmTaskUtil.isCanceledState(task))) && (!(WfmProinsUtil.isCancellationStateProins(WfmTaskUtil.getProInsByTask(task)))) && 
      (isVisible(widget, "link_copysend"))) {
      new ExeTaskBaseState().createUILinkComp(widget, flowh, "link_copysend", "40", null, null, null, null, null);
    }

    if (isVisible(widget, "link_flowimg")) {
      new ExeTaskBaseState().createUILinkComp(widget, flowh, "link_flowimg", "60", null, null, null, null, null);
    }

    if (isVisible(widget, "link_urgencyhistory")) {
      new ExeTaskBaseState().createUILinkComp(widget, flowh, "link_urgencyhistory", "60", null, null, null, null, null);
    }

    if (isVisible(widget, "link_addattach")) {
      new ExeTaskBaseState().createUIAttachAndImageComp(widget, flowh, "60", null, null, null, null, null);
    }
    if ((WfmTaskUtil.allowSupOperate(task)) && (!(WfmTaskUtil.isCanceledState(task))) && (!(WfmProinsUtil.isCancellationStateProins(WfmTaskUtil.getProInsByTask(task)))) && 
      (isVisible(widget, "link_supopinion"))) {
      new ExeTaskBaseState().createUILinkComp(widget, flowh, "link_supopinion", "60", null, null, null, null, null);
    }

    UIFlowvLayout dynFlowv = new UIFlowvLayout();
    dynFlowv.setId("dynflowv");
    flowv.addElementToPanel(dynFlowv);

    UIFlowvPanel dynp1 = new UIFlowvPanel();
    dynp1.setId("dynpanel1");
    dynFlowv.addPanel(dynp1);
    UIFlowhLayout layout_copySend = new UIFlowhLayout();
    layout_copySend.setId("layout_copysend");
    dynp1.setElement(layout_copySend);

    UILabelComp csTextL = new UILabelComp();
    csTextL.setId("label_copysend");
    csTextL.setWidth(null);
    UIFlowhPanel copySend = layout_copySend.addElementToPanel(csTextL);
    copySend.setWidth("50");

    UILabelComp csTextName = new UILabelComp();
    csTextName.setWidth(null);
    csTextName.setId("name_copysend");
    csTextName.setTextAlign("left");
    layout_copySend.addElementToPanel(csTextName);

    UILabelComp csText = new UILabelComp();
    csText.setId("value_copysend");
    layout_copySend.addElementToPanel(csText);

    UIFlowhLayout bottomFlowh = new UIFlowhLayout();
    bottomFlowh.setId("bottomFlowh");
    flowv.addElementToPanel(bottomFlowh);

    ButtonComp recall = (ButtonComp)widget.getViewComponents().getComponent("btn_recall");

    if ((WfmTaskUtil.allowSupOperate(task)) && (!(WfmTaskUtil.isCanceledState(task))) && (!(WfmProinsUtil.isCancellationStateProins(WfmTaskUtil.getProInsByTask(task))))) {
      new ExeTaskBroseState().createUIButton(widget, bottomFlowh, "btn_ok", "64", "right", null, "0", null, "0", null);
    }
    if (recall != null)
    {
      new ExeTaskBroseState().createUIButton(widget, bottomFlowh, "btn_recall", "64", "right", null, "0", null, "0", "blue_button_div");
    }
    ButtonComp urgency = (ButtonComp)widget.getViewComponents().getComponent("$lfw_app_btn_urgency");
    if (urgency == null)
      return;
    new ExeTaskBroseState().createUIButton(widget, bottomFlowh, "$lfw_app_btn_urgency", "64", "right", null, "0", null, "0", null);
  }

  private void createOperator(UIFlowvLayout flowv)
  {
    UIFlowvLayout execFlowh = new UIFlowvLayout();
    execFlowh.setId("execflowh");
    flowv.addElementToPanel(execFlowh);

    UITextField text = new UITextField();
    text.setId("text_exeaction");
    text.setWidth("100%");
    UIFlowvPanel panel1 = execFlowh.addElementToPanel(text);
    panel1.setHeight("50");
    panel1.setId("text_exeaction");
  }

  private void createOpinionAndComLan(LfwView widget, UIFlowvLayout flowv, boolean enable)
  {
    UITextField tf1 = new UITextField();
    tf1.setId("text_commonword1");
    tf1.setWidth("100%");

    flowv.addElementToPanel(tf1);

    UITextField tf2 = new UITextField();
    tf2.setId("text_opinion");
    tf2.setWidth("100%");
    tf2.setHeight("100");
    flowv.addElementToPanel(tf2);

    ComboBoxComp comLan = (ComboBoxComp)widget.getViewComponents().getComponent("text_commonword1");
    comLan.setEnabled(enable);

    TextAreaComp textAreaComp = (TextAreaComp)widget.getViewComponents().getComponent("text_opinion");
    textAreaComp.setEnabled(enable);
  }

  private void setWfmHistory(LfwView widget, UIFlowvLayout contentFlowh)
  {
    UIFlowvLayout historyLayout = new UIFlowvLayout();
    historyLayout.setId("historyLayout");
    contentFlowh.addElementToPanel(historyLayout);

    UIPartComp part = new UIPartComp();
    part.setId("historyWebpart");
    contentFlowh.addElementToPanel(part);

    UIListViewComp uilistComp = new UIListViewComp();
    uilistComp.setId("historyList");

    contentFlowh.addElementToPanel(uilistComp);
  }
}