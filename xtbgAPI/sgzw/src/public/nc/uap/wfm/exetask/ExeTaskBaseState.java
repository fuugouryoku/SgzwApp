package nc.uap.wfm.exetask;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.uap.cpb.log.CpLogger;
import nc.uap.lfw.core.LfwRuntimeEnvironment;
import nc.uap.lfw.core.WebContext;
import nc.uap.lfw.core.base.ExtAttribute;
import nc.uap.lfw.core.comp.ButtonComp;
import nc.uap.lfw.core.comp.ImageComp;
import nc.uap.lfw.core.comp.LabelComp;
import nc.uap.lfw.core.comp.LinkComp;
import nc.uap.lfw.core.event.LinkEvent;
import nc.uap.lfw.core.event.MouseEvent;
import nc.uap.lfw.core.event.conf.EventConf;
import nc.uap.lfw.core.page.LfwView;
import nc.uap.lfw.core.page.ViewComponents;
import nc.uap.lfw.core.uimodel.WindowConfig;
import nc.uap.lfw.file.vo.LfwFileVO;
import nc.uap.lfw.jsp.uimeta.UIButton;
import nc.uap.lfw.jsp.uimeta.UIFlowhLayout;
import nc.uap.lfw.jsp.uimeta.UIFlowhPanel;
import nc.uap.lfw.jsp.uimeta.UIImageComp;
import nc.uap.lfw.jsp.uimeta.UILinkComp;
import nc.uap.wfm.adapter.factory.WfmEngineUIAdapterFactory;
import nc.uap.wfm.adapter.itf.IWfmEngineUIAdapter;
import nc.uap.wfm.utils.WfmAttachUtil;
import nc.uap.wfm.utils.WfmBillUtil;
import nc.uap.wfm.utils.WfmProDefUtil;
import nc.uap.wfm.utils.WfmProinsUtil;
import nc.uap.wfm.utils.WfmTaskUtil;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import org.apache.commons.lang.StringUtils;
import uap.lfw.core.ml.LfwResBundle;
import uap.web.bd.pub.AppUtil;

public class ExeTaskBaseState
{
  public static final String NOTT_START = "NottStart";
  public static final int CompleteSgy_Occupy = 1;

  public void createBinSave(LfwView widget, Object task)
  {
    ButtonComp saveBt = new ButtonComp();
    saveBt.setId("btn_save");
    saveBt.setText(getText("disp_interim", task));
    EventConf savee = MouseEvent.getOnClickEvent();
    savee.setMethodName("btnsave_click");
    saveBt.addEventConf(savee);
    widget.getViewComponents().addComponent(saveBt);
    if (task == null) {
      return;
    }
    if (WfmTaskUtil.isTempSaveMakeBill(task)) {
      saveBt.setEnabled(true);
    }
    else if (WfmTaskUtil.isRunState(task)) {
      saveBt.setEnabled(true);
    }
    else if (WfmTaskUtil.isEndState(task)) {
      saveBt.setEnabled(false);
    }
    else if (WfmTaskUtil.isFinishState(task)) {
      saveBt.setEnabled(false);
    }
    else if (WfmTaskUtil.isPlmntState(task)) {
      saveBt.setEnabled(true);
    }
    else if (WfmTaskUtil.isSuspendedState(task)) {
      saveBt.setEnabled(false);
    }
    else if (WfmTaskUtil.isBeforeAddSignSend(task)) {
      saveBt.setEnabled(false);
    }
    else if (WfmTaskUtil.isBeforeAddSignPlmnt(task)) {
      saveBt.setEnabled(false);
    }
    else if (WfmTaskUtil.isBeforeAddSignCmpltState(task)) {
      saveBt.setEnabled(true);
    }
    else if (WfmTaskUtil.isBeforeAddSignStop(task)) {
      saveBt.setEnabled(true);
    }
    else if (WfmTaskUtil.isUnreadState(task)) {
      saveBt.setEnabled(false);
    }
    else if (WfmTaskUtil.isReadedState(task)) {
      saveBt.setEnabled(false);
    }
    else if (WfmTaskUtil.isReadEndState(task))
      saveBt.setEnabled(false);
  }

  public boolean isOperatable(Object task)
  {
    String curUserID = InvocationInfoProxy.getInstance().getUserId();
    String onwer = WfmTaskUtil.getOwnerPk(task);
    String agentor = WfmTaskUtil.getAgentPk(task);
    return ((curUserID.equals(onwer)) || (curUserID.equals(agentor)));
  }

  public ButtonComp createUrgencyBtn(LfwView widget, Object task, boolean isNC)
  {
    ButtonComp urgencyBtn = new ButtonComp();
    urgencyBtn.setId("$lfw_app_btn_urgency");
    urgencyBtn.setText(getText("disp_urgencybtn", task));
    EventConf oke = MouseEvent.getOnClickEvent();
    oke.setMethodName("btnurgency_click");
    urgencyBtn.addEventConf(oke);
    widget.getViewComponents().addComponent(urgencyBtn);
    if (!(isNC)) {
      if (task == null) {
        return urgencyBtn;
      }
      if (WfmTaskUtil.isTempSaveMakeBill(task)) {
        urgencyBtn.setEnabled(true);
      }
      else if (WfmTaskUtil.isRunState(task)) {
        urgencyBtn.setEnabled(true);
      }
      else if (WfmTaskUtil.isEndState(task)) {
        urgencyBtn.setEnabled(true);
      }
      else if (WfmTaskUtil.isFinishState(task)) {
        urgencyBtn.setEnabled(true);
      }
      else if (WfmTaskUtil.isPlmntState(task)) {
        urgencyBtn.setEnabled(true);
      }
      else if (WfmTaskUtil.isSuspendedState(task)) {
        urgencyBtn.setEnabled(false);
      }
      else if (WfmTaskUtil.isBeforeAddSignSend(task)) {
        urgencyBtn.setEnabled(true);
      }
      else if (WfmTaskUtil.isBeforeAddSignPlmnt(task)) {
        urgencyBtn.setEnabled(true);
      }
      else if (WfmTaskUtil.isBeforeAddSignCmpltState(task)) {
        urgencyBtn.setEnabled(false);
      }
      else if (WfmTaskUtil.isBeforeAddSignStop(task)) {
        urgencyBtn.setEnabled(true);
      }
      else if (WfmTaskUtil.isUnreadState(task)) {
        urgencyBtn.setEnabled(true);
      }
      else if (WfmTaskUtil.isReadedState(task)) {
        urgencyBtn.setEnabled(true);
      }
      else if (WfmTaskUtil.isReadEndState(task)) {
        urgencyBtn.setEnabled(true);
      }
    }
    return urgencyBtn;
  }

  public void createScratchPad(LfwView widget, Object task)
  {
    LinkComp scratchpad = new LinkComp();
    scratchpad.setText(getText("disp_scratchpad", task));
    scratchpad.setI18nName(getText("disp_scratchpad", task));
    scratchpad.setId("text_scratchpad");
    EventConf e1 = LinkEvent.getOnClickEvent();
    e1.setMethodName("scrtchPadClick");
    scratchpad.addEventConf(e1);
    widget.getViewComponents().addComponent(scratchpad);
    if (task == null)
      return;
    if (WfmTaskUtil.isReadEndState(task)) {
      scratchpad.setVisible(false);
    }

    String windowId = "text_scratchpad";
    WindowConfig scratchWindow = new WindowConfig();
    scratchWindow.setId(windowId);
    widget.addInlineWindow(scratchWindow);
  }

  public void createUrgencyHistory(LfwView widget, Object task)
  {
    LinkComp urgencyHistoryLink = new LinkComp();
    urgencyHistoryLink.setText(getText("disp_urgencyhistory", task));

    urgencyHistoryLink.setI18nName(getText("disp_urgencyhistory", task));
    urgencyHistoryLink.setId("link_urgencyhistory");

    EventConf e1 = LinkEvent.getOnClickEvent();
    e1.setMethodName("urgencyHistory");
    urgencyHistoryLink.addEventConf(e1);
    widget.getViewComponents().addComponent(urgencyHistoryLink);

    String windowId = "wfm_urgencyhistory";
    WindowConfig window = new WindowConfig();
    window.setId(windowId);
    widget.addInlineWindow(window);
  }

  public void createOpinionLinkComp(LfwView widget, Object task)
  {
    LinkComp opinionLink = new LinkComp();
    opinionLink.setText(getText("disp_opinion", task));
    if (opinionLink.getText() == null)
      opinionLink.setText(LfwResBundle.getInstance().getStrByID("wfm", "DispStrategy-000088"));
    opinionLink.setI18nName(getText("disp_opinion", task));
    opinionLink.setId("link_opinion");
    EventConf e1 = LinkEvent.getOnClickEvent();
    e1.setMethodName("opinionClick");
    opinionLink.addEventConf(e1);
    widget.getViewComponents().addComponent(opinionLink);
  }

  public void createSubmit(LfwView widget, Object task)
  {
    ButtonComp okBt = new ButtonComp();
    okBt.setId("btn_ok");
    okBt.setText(getText("disp_submit", task));
    EventConf oke = MouseEvent.getOnClickEvent();
    oke.setMethodName("btnok_click");
    okBt.addEventConf(oke);
    widget.getViewComponents().addComponent(okBt);
    boolean isNC = WfmBillUtil.isNCBill();
    if (isNC) {
      String state = (String)AppUtil.getAppAttr("NCState");
      if ("State_End".equals(state))
        okBt.setVisible(false);
    }
    else
    {
      if (task == null) {
        return;
      }

      if (WfmTaskUtil.isTempSaveMakeBill(task)) {
        okBt.setEnabled(true);
      }
      else if (WfmTaskUtil.isRunState(task)) {
        okBt.setEnabled(true);
      }
      else if (WfmTaskUtil.isEndState(task)) {
        okBt.setEnabled(true);
      }
      else if (WfmTaskUtil.isFinishState(task)) {
        okBt.setEnabled(true);
      }
      else if (WfmTaskUtil.isPlmntState(task)) {
        okBt.setEnabled(true);
      }
      else if (WfmTaskUtil.isSuspendedState(task)) {
        okBt.setEnabled(false);
      }
      else if (WfmTaskUtil.isBeforeAddSignSend(task)) {
        okBt.setEnabled(false);
      }
      else if (WfmTaskUtil.isBeforeAddSignPlmnt(task)) {
        okBt.setEnabled(false);
      }
      else if (WfmTaskUtil.isBeforeAddSignCmpltState(task)) {
        okBt.setEnabled(true);
      }
      else if (WfmTaskUtil.isBeforeAddSignStop(task)) {
        okBt.setEnabled(true);
      }
      else if (WfmTaskUtil.isUnreadState(task)) {
        okBt.setEnabled(true);
      }
      else if (WfmTaskUtil.isReadedState(task)) {
        okBt.setEnabled(true);
      }
      else if (WfmTaskUtil.isReadEndState(task))
        okBt.setEnabled(true);
    }
  }

  public void createAttachFile(LfwView widget, Object task)
  {
    LinkComp link = new LinkComp();
    link.setText(getText("disp_attachfile", task));
    link.setI18nName(getText("disp_attachfile", task));
    link.setId("link_addattach");
    EventConf e1 = LinkEvent.getOnClickEvent();
    e1.setMethodName("attachClick");
    link.addEventConf(e1);
    widget.getViewComponents().addComponent(link);
    LfwFileVO[] files = getFile(task);
    if ((files != null) && (files.length > 0)) {
      ImageComp attachImage = new ImageComp();
      attachImage.setId("image_addattach");
      attachImage.setAlt(files.length + "");
      attachImage.setImage1("images/attach.png");
      widget.getViewComponents().addComponent(attachImage);
    }

    String windowId = "filemgr";
    WindowConfig attachWindow = new WindowConfig();
    attachWindow.setId(windowId);
    widget.addInlineWindow(attachWindow);

    LabelComp label_attach = new LabelComp();
    label_attach.setId("label_attach");
    label_attach.setText(NCLangRes4VoTransl.getNCLangRes().getStrByID("wfm", "ExecuteTaskWidgetProvider-000016"));
    widget.getViewComponents().addComponent(label_attach);

    LabelComp value_attach = new LabelComp();
    value_attach.setId("value_attach");
    value_attach.setText("----");
    widget.getViewComponents().addComponent(value_attach);
  }

  private LfwFileVO[] getFile(Object task)
  {
    LfwFileVO[] files = null;
    String taskPk = null;
    taskPk = WfmTaskUtil.getTaskPkFromSession();
    String sysid = (String)AppUtil.getAppAttr("sysid");
    if (sysid == null) {
      sysid = "bafile";
    }
    String billItem = WfmAttachUtil.getBillItem();
    files = WfmAttachUtil.getAttachFilesByBillItem(billItem, sysid);
    boolean isNC = WfmBillUtil.isNCBill();
    if (!(isNC)) {
      files = WfmAttachUtil.filteRuningAttaches(files, taskPk, false);
    }
    return files;
  }

  public void createUIAttachAndImageComp(LfwView widget, UIFlowhLayout flowh, String defalutLen, String floatStr, String rightPadding, String topPadding, String leftPadding, String bottomPadding) {
    Object attachImageCompObj = widget.getViewComponents().getComponent("image_addattach");
    if (attachImageCompObj != null) {
      UIImageComp uIImageComp = new UIImageComp();
      uIImageComp.setId("image_addattach");
      UIFlowhPanel uIFlowhPanel = flowh.addElementToPanel(uIImageComp);
      uIFlowhPanel.setId("image_addattach");
      uIFlowhPanel.setRightPadding("-20");
      uIFlowhPanel.setWidth("18");
      uIFlowhPanel.setLeftPadding(null);
    }

    createUILinkComp(widget, flowh, "link_addattach", "60", floatStr, rightPadding, topPadding, leftPadding, bottomPadding);
  }

  public void createReCall(LfwView widget, Object task, boolean isNC)
  {
    ButtonComp recallBt = new ButtonComp();
    recallBt.setId("btn_recall");
    recallBt.setText(getText("disp_back", task));
    EventConf oke = MouseEvent.getOnClickEvent();
    oke.setMethodName("btnrecall_click");
    recallBt.addEventConf(oke);
    widget.getViewComponents().addComponent(recallBt);

    if (isNC) {
      String state = (String)AppUtil.getAppAttr("NCState");
      if ("State_Run".equals(state)) {
        recallBt.setVisible(false);
      }
    }
    else if ((task != null) && (WfmTaskUtil.isCallBackEnable(task))) {
      Object proins = WfmTaskUtil.getProInsByTask(task);
      Object prodef = WfmTaskUtil.getProDefByTask(task);
      if (WfmProinsUtil.isEndStateProins(proins))
      {
        if (WfmProDefUtil.isProDefAllowBack(prodef))
          recallBt.setEnabled(true);
        else {
          recallBt.setEnabled(false);
        }

      }
      else if (WfmTaskUtil.allowReverseAudit(task))
        recallBt.setEnabled(true);
      else
        recallBt.setEnabled(false);
    }
  }

  public void createFlowImg(LfwView widget, Object task)
  {
    if (LfwRuntimeEnvironment.getWebContext().getParameter("workflowtype") != null) {
      return;
    }
    LinkComp flowImgLink = new LinkComp();
    flowImgLink.setText(getText("disp_flowimg", task));
    flowImgLink.setI18nName(getText("disp_flowimg", task));
    flowImgLink.setId("link_flowimg");
    EventConf le3 = LinkEvent.getOnClickEvent();
    le3.setMethodName("flowImgClick");
    flowImgLink.addEventConf(le3);
    widget.getViewComponents().addComponent(flowImgLink);

    if (WfmTaskUtil.isTempSaveMakeBill(task)) {
      flowImgLink.setEnabled(true);
    }
    else if (WfmTaskUtil.isRunState(task)) {
      flowImgLink.setEnabled(true);
    }
    else if (WfmTaskUtil.isEndState(task)) {
      flowImgLink.setEnabled(true);
    }
    else if (WfmTaskUtil.isFinishState(task)) {
      flowImgLink.setEnabled(true);
    }
    else if (WfmTaskUtil.isPlmntState(task)) {
      flowImgLink.setEnabled(true);
    }
    else if (WfmTaskUtil.isSuspendedState(task)) {
      flowImgLink.setEnabled(true);
    }
    else if (WfmTaskUtil.isBeforeAddSignSend(task)) {
      flowImgLink.setEnabled(true);
    }
    else if (WfmTaskUtil.isBeforeAddSignPlmnt(task)) {
      flowImgLink.setEnabled(true);
    }
    else if (WfmTaskUtil.isBeforeAddSignCmpltState(task)) {
      flowImgLink.setEnabled(true);
    }
    else if (WfmTaskUtil.isBeforeAddSignStop(task)) {
      flowImgLink.setEnabled(true);
    }
    else if (WfmTaskUtil.isUnreadState(task)) {
      flowImgLink.setEnabled(true);
    }
    else if (WfmTaskUtil.isReadedState(task)) {
      flowImgLink.setEnabled(true);
    }
    else if (WfmTaskUtil.isReadEndState(task)) {
      flowImgLink.setEnabled(true);
    }

    String windowId = "wfm_flowhistory";
    WindowConfig copyWindow = new WindowConfig();
    boolean isNC = WfmBillUtil.isNCBill();
    if (isNC)
      windowId = "pfinfo";
    copyWindow.setId(windowId);
    widget.addInlineWindow(copyWindow);
  }

  protected String getText(String key, Object task)
  {
    return WfmEngineUIAdapterFactory.getInstance().getTaskDispName(key, task); }

  public void createAfterAddSign(LfwView widget, Object task) {
    LinkComp afterAddSign = new LinkComp();
    afterAddSign.setId("link_aftaddsign");
    afterAddSign.setText(getText("disp_afteraddsign", task));
    afterAddSign.setI18nName(getText("disp_afteraddsign", task));
    EventConf lae = LinkEvent.getOnClickEvent();
    lae.setMethodName("afterAddSignClick");
    afterAddSign.addEventConf(lae);
    widget.getViewComponents().addComponent(afterAddSign);

    LabelComp aftaddSignText = new LabelComp();
    aftaddSignText.setText(NCLangRes4VoTransl.getNCLangRes().getStrByID("wfm", "ExecuteTaskWidgetProvider-000017") + ":");
    aftaddSignText.setId("label_afteraddsign");
    widget.getViewComponents().addComponent(aftaddSignText);

    LabelComp value_afterAddSign = new LabelComp();
    value_afterAddSign.setId("value_afteraddsign");
    value_afterAddSign.setText("");
    value_afterAddSign.setVisible(false);
    widget.getViewComponents().addComponent(value_afterAddSign);

    LabelComp name_afterAddSign = new LabelComp();
    name_afterAddSign.setId("name_afteraddsign");
    name_afterAddSign.setText("");
    widget.getViewComponents().addComponent(name_afterAddSign);

    if (WfmTaskUtil.isTempSaveMakeBill(task)) {
      afterAddSign.setEnabled(true);
    }
    else if (WfmTaskUtil.isRunState(task)) {
      afterAddSign.setEnabled(true);
    }
    else if (WfmTaskUtil.isEndState(task)) {
      afterAddSign.setEnabled(false);
    }
    else if (WfmTaskUtil.isFinishState(task)) {
      afterAddSign.setEnabled(true);
    }
    else if (WfmTaskUtil.isPlmntState(task)) {
      afterAddSign.setEnabled(true);
    }
    else if (WfmTaskUtil.isSuspendedState(task)) {
      afterAddSign.setEnabled(false);
    }
    else if (WfmTaskUtil.isBeforeAddSignSend(task)) {
      afterAddSign.setEnabled(false);
    }
    else if (WfmTaskUtil.isBeforeAddSignPlmnt(task)) {
      afterAddSign.setEnabled(false);
    }
    else if (WfmTaskUtil.isBeforeAddSignCmpltState(task)) {
      afterAddSign.setEnabled(true);
    }
    else if (WfmTaskUtil.isBeforeAddSignStop(task)) {
      afterAddSign.setEnabled(true);
    }
    else if (WfmTaskUtil.isUnreadState(task)) {
      afterAddSign.setEnabled(false);
    }
    else if (WfmTaskUtil.isReadedState(task)) {
      afterAddSign.setEnabled(false);
    }
    else if (WfmTaskUtil.isReadEndState(task)) {
      afterAddSign.setEnabled(false);
    }
    if (task != null) {
      if (!(WfmTaskUtil.allowBeforeAddSign(task))) {
        afterAddSign.setEnabled(false);
      }
      if (WfmTaskUtil.isBeforeAddSignCreatedTask(task))
        afterAddSign.setEnabled(false);
    }
    else
    {
      String billStatus = (String)AppUtil.getAppAttr("billStatus");
      if ("NottStart".equals(billStatus)) {
        afterAddSign.setEnabled(false);
      }

    }

    String windowId = "aftaddsignpm";
    WindowConfig scratchWindow = new WindowConfig();
    scratchWindow.setId(windowId);
    widget.addInlineWindow(scratchWindow);
  }

  protected boolean isVisible(LfwView widget, String compid)
  {
    boolean visible = true;
    ExtAttribute attr = widget.getExtendAttribute(compid);
    if ((attr != null) && ("false".equals(attr.getValue()))) {
      visible = false;
    }
    return visible;
  }

  public String getLabelLen(String text, String defaultvalue)
  {
    text = (text == null) ? "" : text;

    int strLen = text.getBytes().length * 6 + 15;
    if (StringUtils.isBlank(defaultvalue)) defaultvalue = "0";
    int defaultvalueLen = Integer.valueOf(defaultvalue).intValue();

    if (defaultvalueLen > 0) {
      strLen = (strLen > defaultvalueLen) ? defaultvalueLen : strLen;
    }
    String len = String.valueOf(strLen);

    return len;
  }

  public String getBtnLen(String text, String defaultvalue)
  {
    return ((text != null) ? String.valueOf(12 * text.length() + 40) : defaultvalue);
  }

  public void createUILinkComp(LfwView widget, UIFlowhLayout flowh, String elementId, String defalutLen, String floatStr, String rightPadding, String topPadding, String leftPadding, String bottomPadding)
  {
    LinkComp linkComp = (LinkComp)widget.getViewComponents().getComponent(elementId);
    if (linkComp == null) return;
    UILinkComp uILinkComp = new UILinkComp();
    uILinkComp.setId(elementId);
    UIFlowhPanel uIFlowhPanel = flowh.addElementToPanel(uILinkComp);
    if (linkComp != null) {
      String len = getLabelLen(linkComp.getText(), defalutLen);
      len = String.valueOf(Integer.valueOf(len));
      uIFlowhPanel.setWidth(len);

      uILinkComp.setWidth(len);
      uIFlowhPanel.setId(elementId);
      if (!(StringUtils.isEmpty(floatStr)))
        uIFlowhPanel.setFloat(floatStr);
      if (!(StringUtils.isEmpty(rightPadding)))
        uIFlowhPanel.setRightPadding(rightPadding);
      if (!(StringUtils.isEmpty(topPadding)))
        uIFlowhPanel.setTopPadding(topPadding);
      if (!(StringUtils.isEmpty(leftPadding)))
        uIFlowhPanel.setLeftPadding(leftPadding);
      if (!(StringUtils.isEmpty(bottomPadding)))
        uIFlowhPanel.setBottomPadding(bottomPadding);
      createEmptyUIFlowhPanel(flowh, "15");
    }
  }

  public void createCopySend(LfwView widget, Object task, boolean isNC)
  {
    LinkComp sendCopy = new LinkComp();
    sendCopy.setText(getText("disp_sendcopy", task));
    sendCopy.setI18nName(getText("disp_sendcopy", task));
    sendCopy.setId("link_copysend");
    EventConf e1 = LinkEvent.getOnClickEvent();
    e1.setMethodName("deliverClick");
    sendCopy.addEventConf(e1);
    widget.getViewComponents().addComponent(sendCopy);

    LabelComp linkLabel = new LabelComp();
    linkLabel.setId("label_copysend");
    linkLabel.setText(getText("disp_sendcopy", task) + ":");
    widget.getViewComponents().addComponent(linkLabel);

    LabelComp linkValue = new LabelComp();
    linkValue.setId("value_copysend");
    linkValue.setText("");
    widget.getViewComponents().addComponent(linkValue);

    LabelComp lableName = new LabelComp();
    lableName.setId("name_copysend");
    lableName.setText("");
    lableName.setVisible(true);
    widget.getViewComponents().addComponent(lableName);

    if (WfmTaskUtil.isTempSaveMakeBill(task)) {
      sendCopy.setEnabled(true);
    }
    else if (WfmTaskUtil.isRunState(task)) {
      sendCopy.setEnabled(true);
    }
    else if (WfmTaskUtil.isEndState(task)) {
      sendCopy.setEnabled(false);
    }
    else if (WfmTaskUtil.isFinishState(task)) {
      sendCopy.setEnabled(true);
    }
    else if (WfmTaskUtil.isPlmntState(task)) {
      sendCopy.setEnabled(true);
    }
    else if (WfmTaskUtil.isSuspendedState(task)) {
      sendCopy.setEnabled(false);
    }
    else if (WfmTaskUtil.isBeforeAddSignSend(task)) {
      sendCopy.setEnabled(false);
    }
    else if (WfmTaskUtil.isBeforeAddSignPlmnt(task)) {
      sendCopy.setEnabled(false);
    }
    else if (WfmTaskUtil.isBeforeAddSignCmpltState(task)) {
      sendCopy.setEnabled(true);
    }
    else if (WfmTaskUtil.isBeforeAddSignStop(task)) {
      sendCopy.setEnabled(true);
    }
    else if (WfmTaskUtil.isUnreadState(task)) {
      sendCopy.setEnabled(true);
    }
    else if (WfmTaskUtil.isReadedState(task)) {
      sendCopy.setEnabled(true);
    }
    else if (WfmTaskUtil.isReadEndState(task)) {
      sendCopy.setEnabled(false);
    }

    if (task != null) {
      if ((!(WfmTaskUtil.allowDeliver(task))) && (!(isNC))) {
        sendCopy.setEnabled(false);
      }
      else
        sendCopy.setEnabled(true);
    }
    else
    {
      String billStatus = null;
      try {
        billStatus = (String)AppUtil.getAppAttr("billStatus");
      } catch (Exception e) {
        CpLogger.error(e.getMessage(), e);
      }
      if ("NottStart".equals(billStatus))
      {
        sendCopy.setEnabled(false);
      }

    }

    String windowId = "deliverpm";
    WindowConfig deliverWindow = new WindowConfig();
    deliverWindow.setId(windowId);
    widget.addInlineWindow(deliverWindow);
  }

  public UIFlowhPanel createEmptyUIFlowhPanel(UIFlowhLayout flowh, String width)
  {
    UIFlowhPanel emptyPanel = new UIFlowhPanel();
    emptyPanel.setWidth(width);
    flowh.addPanel(emptyPanel);
    return emptyPanel;
  }

  public UIFlowhPanel createUIButton(LfwView widget, UIFlowhLayout flowh, String elementId, String defalutLen, String floatStr, String rightPadding, String topPadding, String leftPadding, String bottomPadding, String className)
  {
    UIButton uIButton = new UIButton();
    uIButton.setId(elementId);
    if (!(StringUtils.isEmpty(className)))
      uIButton.setClassName(className);
    ButtonComp buttonComp = (widget.getViewComponents().getComponent(elementId) == null) ? null : (ButtonComp)widget.getViewComponents().getComponent(elementId);
    String len = getBtnLen((buttonComp == null) ? "NULL" : buttonComp.getText(), defalutLen);
    uIButton.setWidth(len);

    UIFlowhPanel uIFlowhPanel = flowh.addElementToPanel(uIButton);
    uIFlowhPanel.setWidth((Integer.parseInt(len) + 10) + "");
    uIFlowhPanel.setId(elementId);
    if (!(StringUtils.isEmpty(floatStr)))
      uIFlowhPanel.setFloat(floatStr);
    if (!(StringUtils.isEmpty(rightPadding)))
      uIFlowhPanel.setRightPadding(rightPadding);
    if (!(StringUtils.isEmpty(topPadding)))
      uIFlowhPanel.setTopPadding(topPadding);
    if (!(StringUtils.isEmpty(leftPadding)))
      uIFlowhPanel.setLeftPadding(leftPadding);
    if (!(StringUtils.isEmpty(bottomPadding)))
      uIFlowhPanel.setBottomPadding(bottomPadding);
    return uIFlowhPanel;
  }
}