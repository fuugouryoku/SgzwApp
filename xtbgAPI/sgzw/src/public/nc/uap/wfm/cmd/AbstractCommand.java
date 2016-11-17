package nc.uap.wfm.cmd;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nc.bs.framework.common.NCLocator;
import nc.uap.lfw.core.exception.LfwRuntimeException;
import nc.uap.lfw.file.FileManager;
import nc.uap.lfw.file.vo.LfwFileVO;
import nc.uap.wfm.constant.WfmConstants;
import nc.uap.wfm.contanier.ProDefsContainer;
import nc.uap.wfm.context.HumActInfoEngCtx;
import nc.uap.wfm.context.HumActInfoPageCtx;
import nc.uap.wfm.context.NextTaskInfoCtx;
import nc.uap.wfm.context.PwfmContext;
import nc.uap.wfm.context.PwfmContext.BpmnSession;
import nc.uap.wfm.engine.IFlowRequest;
import nc.uap.wfm.engine.IFlowResponse;
import nc.uap.wfm.engine.ILogicDecision;
import nc.uap.wfm.exception.WfmServiceException;
import nc.uap.wfm.exe.WfmParams;
import nc.uap.wfm.execution.HumActInsExecution;
import nc.uap.wfm.execution.ProInsExecution;
import nc.uap.wfm.execution.TaskExecution;
import nc.uap.wfm.handler.EndEventHandler;
import nc.uap.wfm.handler.GateWayLogicHandler;
import nc.uap.wfm.handler.HumActInsHandler;
import nc.uap.wfm.handler.HumActLogicHandler;
import nc.uap.wfm.handler.PortAndEdgeHandler;
import nc.uap.wfm.itf.IWfmHumActInsQry;
import nc.uap.wfm.itf.IWfmTaskBill;
import nc.uap.wfm.itf.IWfmTaskQry;
import nc.uap.wfm.logger.WfmLogger;
import nc.uap.wfm.model.Activity;
import nc.uap.wfm.model.EndEvent;
import nc.uap.wfm.model.Event;
import nc.uap.wfm.model.GateWay;
import nc.uap.wfm.model.HumAct;
import nc.uap.wfm.model.HumActIns;
import nc.uap.wfm.model.IEdge;
import nc.uap.wfm.model.IGraphElement;
import nc.uap.wfm.model.IPort;
import nc.uap.wfm.model.ManAct;
import nc.uap.wfm.model.ProDef;
import nc.uap.wfm.model.ProIns;
import nc.uap.wfm.model.RecAct;
import nc.uap.wfm.model.ScrAct;
import nc.uap.wfm.model.SequenceFlow;
import nc.uap.wfm.model.SingleInOutPort;
import nc.uap.wfm.model.Task;
import nc.uap.wfm.runtime.NextHumActInfoUtil;
import nc.uap.wfm.server.BizProcessServer;
import nc.uap.wfm.utils.WfmAttachUtil;
import nc.uap.wfm.utils.WfmCPTaskUtil;
import nc.uap.wfm.utils.WfmCPUtilFacade;
import nc.uap.wfm.utils.WfmClassUtil;
import nc.uap.wfm.vo.WfmFormInfoCtx;
import nc.uap.wfm.vo.WfmTaskVO;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.lang.UFBoolean;
import org.apache.commons.lang.StringUtils;
import uap.lfw.core.locator.ServiceLocator;

public abstract class AbstractCommand
{
  protected ProInsExecution proInsExe;
  protected HumActInsExecution humActInsExe;
  protected TaskExecution taskExe;

  public AbstractCommand()
  {
    this.proInsExe = ProInsExecution.getInstance();

    this.humActInsExe = HumActInsExecution.getInstance();

    this.taskExe = TaskExecution.getInstance();
  }

  public void signal(ProIns pProIns, ProIns rProIns, HumActIns pHumActIns, IPort[] nextPorts, IGraphElement[] ges, Task pTask)
    throws WfmServiceException
  {
    for (int i = 0; i < ges.length; ++i) {
      IPort port =null;
      IGraphElement o = ges[i];
      if (o instanceof IPort) {
        port = (IPort)o;
        if (port instanceof GateWay) {
          GateWay gateWay = (GateWay)port;
          IEdge[] outEdges= null;
          IEdge[] inEdges = gateWay.getInEdges();
          if (GateWayLogicHandler.gateWayLogicJudge(gateWay, pTask))
          {
            completeHumInsAndTask(pTask, inEdges);
             outEdges = PortAndEdgeHandler.getOutEdges(port);
            signal(pProIns, rProIns, pHumActIns, nextPorts, outEdges, pTask);
          }
          else {
            Set subHumActIns = ((IWfmHumActInsQry)ServiceLocator.getService(IWfmHumActInsQry.class)).getSubHumActInsByParentPk(pTask.getHumActIns().getPk_humactins());
            if ((subHumActIns == null) || (subHumActIns.size() == 0)) {
              HumActIns currHumActIns = new HumActIns();
              currHumActIns.setIsNotReject(UFBoolean.FALSE);
              currHumActIns.setIsNotExe(UFBoolean.TRUE);
              currHumActIns.setIsNotPas(UFBoolean.TRUE);
              currHumActIns.setPhumact_id(pTask.getHumActIns().getHumact_id());
              currHumActIns.setPk_proins(pTask.getPk_proIns());
              currHumActIns.setPk_rootproins(pTask.getPk_rootProIns());
              currHumActIns.setPPort(pTask.getPort());
              currHumActIns.setParent(pTask.getHumActIns());
              currHumActIns.setPort(port);
              currHumActIns.setState("End");
              currHumActIns.asyn();
              WfmLogger.info("插入人工活动信息---AbstractCommand"); }
          }
        } else {
          if (port instanceof Activity) {
        	  IEdge[] outEdges=null;
            if (port instanceof ScrAct) {
              outEdges = PortAndEdgeHandler.getOutEdges(port);
              HumActIns newHumActIns = new HumActInsHandler((ScrAct)port, nextPorts, pProIns, rProIns, pHumActIns, pTask).handler();
              pTask = ((Task[])newHumActIns.getTasks().toArray(new Task[0]))[0];
              autoExeScrAct(pTask);
              signal(pProIns, rProIns, pHumActIns, nextPorts, outEdges, pTask);
              break ; } if (port instanceof HumAct) {
              HumAct humact = (HumAct)port;
              Task task = PwfmContext.getCurrentBpmnSession().getTask();

              if (HumActLogicHandler.isAllPreComplete(humact, task))
              {
                HumActIns newHumActIns = new HumActInsHandler(humact, nextPorts, pProIns, rProIns, pHumActIns, pTask).handler();

                if (newHumActIns != null) {
                  nextPorts = jumpHumact(pProIns, rProIns, nextPorts, port, humact, task, newHumActIns);
                }

                if ((StringUtils.isNotBlank(((HumAct)port).getActionType())) && ("Deliver".equalsIgnoreCase(((HumAct)port).getActionType())) && (PortAndEdgeHandler.isContainPort(nextPorts, port)))
                {
                  nextPorts = handleReadHumact(pProIns, rProIns, pTask, i, port, newHumActIns);
                }
              }
              break ; } if (port instanceof ManAct) {
              outEdges = PortAndEdgeHandler.getOutEdges(port);
              signal(pProIns, rProIns, pHumActIns, nextPorts, outEdges, pTask);
              break; } if (!(port instanceof RecAct)) break;
             outEdges = PortAndEdgeHandler.getOutEdges(port);
            signal(pProIns, rProIns, pHumActIns, nextPorts, outEdges, pTask);
            continue;
          }
          if (port instanceof Event) {
            if ((!(port instanceof EndEvent)) || 
              (!(PortAndEdgeHandler.isContainPort(nextPorts, port)))) break ;
            new EndEventHandler(pTask, port, pHumActIns, pProIns, rProIns).handler();

            if (pProIns.getPproIns() == null) break ;
            String pk_parent_prodef = pProIns.getPproIns().getProDef().getPk_prodef();
            ProDef parent = ProDefsContainer.getProDefByProdefPk(pk_parent_prodef);
            String key = pProIns.getProDef().getPk_prodef();
            IPort currentPort = (IPort)parent.getPorts().get(key);
            pHumActIns = ((IWfmHumActInsQry)NCLocator.getInstance().lookup(IWfmHumActInsQry.class)).getHumActInsByProInsPkAndPrtId(pProIns.getPproIns().getPk_proins(), key);
            pHumActIns.setIsNotPas(UFBoolean.TRUE);
            if ("Exe".equalsIgnoreCase(pHumActIns.getState())) {
              pHumActIns.setState("End");
            }
            pHumActIns.asyn();
            pTask = (Task)((IWfmTaskQry)NCLocator.getInstance().lookup(IWfmTaskQry.class)).getTasksByHumActInsPk(pHumActIns.getPk_humactins()).iterator().next();
            if (HumActLogicHandler.isAllPreComplete((SingleInOutPort)currentPort, pTask)) {
              IEdge[] outEdges = PortAndEdgeHandler.getOutEdges(currentPort);
              nextPorts = PortAndEdgeHandler.getNextHumActs(currentPort);

              PwfmContext.getCurrentBpmnSession().setTask(pTask);

              signal(pProIns.getPproIns(), rProIns, pHumActIns, nextPorts, outEdges, pTask);
            }
          }
        }
      }
      if ((port instanceof ProDef) && 
        (!(o instanceof IEdge))) continue;
      SequenceFlow sf = (SequenceFlow)o;
      ILogicDecision logic = (ILogicDecision)WfmClassUtil.loadClass(sf.getSelfDefClass());
      WfmFormInfoCtx formVo = PwfmContext.getCurrentBpmnSession().getFormVo();
      if (logic.judge(pTask, sf, formVo))
        signal(pProIns, rProIns, pHumActIns, nextPorts, PortAndEdgeHandler.getTargetPorts(sf), pTask);
    }
  }

  private IPort[] handleReadHumact(ProIns pProIns, ProIns rProIns, Task pTask, int i, IPort port, HumActIns newHumActIns)
    throws WfmServiceException
  {
    IEdge[] outEdges = PortAndEdgeHandler.getOutEdges((HumAct)port);
    WfmParams wfmParams = new WfmParams();

    String taskPk = ((Task)newHumActIns.getTasks().iterator().next()).getPk_task();
    wfmParams.setTaskPk(taskPk);
    wfmParams.setFormInfoCtx(PwfmContext.getCurrentBpmnSession().getFormVo());
    List nextinfo = new NextHumActInfoUtil().getNextHumActInfo(wfmParams);
    int length = nextinfo.size();
    boolean isAssign = length > 1;
    ProDef proDef = PwfmContext.getCurrentBpmnSession().getProDef();
    Map ports = proDef.getPorts();
    List list = new ArrayList();
    IPort tport = null;
    for (int k = 0; k < length; ++k) {
      if ((((HumActInfoPageCtx)nextinfo.get(i)).isAssign()) && (!(isAssign))) {
        isAssign = true;
      }

      tport = (IPort)ports.get(((HumActInfoPageCtx)nextinfo.get(i)).getPortId());
      list.add(tport);
    }
    if (isAssign) {
      throw new LfwRuntimeException(NCLangRes4VoTransl.getNCLangRes().getStrByID("wfm", "AbstractCommand-000001"));
    }
    IPort[] nextPorts = (IPort[])list.toArray(new IPort[0]);
    signal(pProIns, rProIns, newHumActIns, nextPorts, outEdges, pTask);
    return nextPorts;
  }

  private IPort[] jumpHumact(ProIns pProIns, ProIns rProIns, IPort[] nextPorts, IPort port, HumAct humact, Task task, HumActIns newHumActIns)
    throws WfmServiceException
  {
    String isJump = newHumActIns.getHumAct().getAllowMerge();

    if ((UFBoolean.valueOf(isJump).booleanValue()) && ("Normal".equalsIgnoreCase(humact.getActionType())) && (nextPorts.length == 1) && (newHumActIns.getTasks().size() == 1) && ("Normal".equalsIgnoreCase(task.getHumActIns().getHumAct().getActionType())))
    {
      HumActIns humActIns = task.getHumActIns();
      if (humActIns.getTasks().size() > 0) {
        Task tempTask = (Task)newHumActIns.getTasks().iterator().next();
        if (task.getPk_owner().equalsIgnoreCase(tempTask.getPk_owner())) {
          WfmParams wfmParams = new WfmParams();
          wfmParams.setTaskPk(tempTask.getPk_task());
          wfmParams.setFormInfoCtx(PwfmContext.getCurrentBpmnSession().getFormVo());
          List<HumActInfoPageCtx> nextinfo = new NextHumActInfoUtil().getNextHumActInfo(wfmParams);
          if ((nextinfo != null) && (nextinfo.size() > 0) && (!(WfmCPUtilFacade.needAssign(nextinfo)))) {
            if (((HumActInfoPageCtx)nextinfo.get(0)).isAssign()) {
              PwfmContext.getCurrentBpmnSession().getResponse().setAttribute("tipFlag", "autoTip");
            } else {
              List list = new ArrayList();
              boolean isContaiEnd = false;
              for (HumActInfoPageCtx humActInfoPageCtx : nextinfo) {
                ProDef proDef = PwfmContext.getCurrentBpmnSession().getProDef();
                Map ports = proDef.getPorts();
                IPort tport = (IPort)ports.get(humActInfoPageCtx.getPortId());
                if (tport instanceof EndEvent) {
                  list.add(tport);
                  isContaiEnd = true;
                } else {
                  String userPks = humActInfoPageCtx.getUserPks();
                  if (!(StringUtils.isBlank(userPks))) {
                    list.add(tport);
                  }
                }
              }
              if (list.size() > 0) {
                nextPorts = (IPort[])list.toArray(new IPort[0]);
                if ((list.size() == 1) && (list.get(0) instanceof EndEvent))
                {
                  handleJumpTaskAndHumact(tempTask);
                  addReturnFalgStr("autosuccess_tip");

                  new EndEventHandler(tempTask, nextPorts[0], newHumActIns, pProIns, rProIns).handler();
                }
                else if ((list.size() > 0) && (!(isContaiEnd)))
                {
                  handleJumpTaskAndHumact(tempTask);
                  addReturnFalgStr("autosuccess_tip");

                  IEdge[] outEdges = PortAndEdgeHandler.getOutEdges(port);
                  signal(pProIns, rProIns, newHumActIns, nextPorts, outEdges, tempTask);
                }
              }
            }
          }
        }
      }
    }

    return nextPorts; }

  public void addReturnFalgStr(String nowTip) {
    Object returnFlag = PwfmContext.getCurrentBpmnSession().getResponse().getAttribute("tipFlag");
    String tempStr = (returnFlag == null) ? "" : (String)returnFlag;
    tempStr = tempStr + nowTip;
    PwfmContext.getCurrentBpmnSession().getResponse().setAttribute("tipFlag", tempStr);
  }

  private void handleJumpTaskAndHumact(Task tempTask)
    throws WfmServiceException
  {
    new CompleteTaskCmd(tempTask).execute();
    tempTask.setFinishType("FinishType_Auto");

    String opinionStr = NCLangRes4VoTransl.getNCLangRes().getStrByID("wfm", "AbstractCommand-z20001");
    tempTask.setOpinion(opinionStr);
    tempTask.asyn();

    new CompleteHumActInsCmd(tempTask).execute();
  }

  private void completeHumInsAndTask(Task task, IEdge[] inEdges)
  {
    IWfmTaskQry qry = (IWfmTaskQry)NCLocator.getInstance().lookup(IWfmTaskQry.class);
    IWfmTaskBill taskBill = (IWfmTaskBill)NCLocator.getInstance().lookup(IWfmTaskBill.class);
    IWfmHumActInsQry humInsQry = (IWfmHumActInsQry)NCLocator.getInstance().lookup(IWfmHumActInsQry.class);
    Set pHumActIds = new HashSet();
    if ((inEdges != null) && (inEdges.length > 0)) {
      Set<IPort> ports = new HashSet();
      PortAndEdgeHandler.getAllBeforeHumActs(ports, inEdges);
      for (IPort item : ports) {
        pHumActIds.add(item.getId());
      }
      ports.clear();
      ports = null;
    }

    try
    {
      String pk_proins = task.getProIns().getPk_proins();
      WfmTaskVO[] tasks = qry.getRunTasksByProInsPk(pk_proins);
      if ((tasks != null) && (tasks.length > 0)) {
        for (WfmTaskVO vo : tasks) {
          if (task.getPk_task().equals(vo.getPk_task()))
            continue;
          if (!(StringUtils.isNotBlank(vo.getPort_id()))) continue; if (!(pHumActIds.contains(vo.getPort_id()))) {
            continue;
          }
          vo.setFinishtype("FinishType_GateWay");
          if ((!("State_End".equals(vo.getState()))) && (!("CreateType_Deliver".equals(vo.getState())))) {
            vo.setState("State_Canceled");
          }
          taskBill.saveOrUpate(vo);
        }

      }

      Set<HumActIns> humActIns = humInsQry.getHumActInsesByProInsPk(pk_proins);
      if ((humActIns != null) && (humActIns.size() > 0))
        for (HumActIns thumActIns : humActIns) {
          if ((thumActIns.getPort() == null) || (!(StringUtils.isNotBlank(thumActIns.getPort().getId())))) continue; if (!(pHumActIds.contains(thumActIns.getPort().getId()))) {
            continue;
          }
          if (("Run".equals(thumActIns.getState())) || ("Exe".equals(thumActIns.getState()))) {
            thumActIns.setState("Stop");
            thumActIns.asyn();
          }
        }
    }
    catch (WfmServiceException e) {
      WfmLogger.error(e);
      throw new LfwRuntimeException(e);
    }
  }

  private void autoExeScrAct(Task pTask) throws WfmServiceException
  {
    NextTaskInfoCtx flowInfoCtx = new NextTaskInfoCtx();
    flowInfoCtx.setTaskPk(pTask.getPk_task());
    flowInfoCtx.setCntUserPk(PwfmContext.getCurrentBpmnSession().getCurrentUserPk());
    WfmParams wfmParams = new WfmParams();
    wfmParams.setTaskPk(pTask.getPk_task());
    wfmParams.setFormInfoCtx(PwfmContext.getCurrentBpmnSession().getFormVo());
    List pageCtx = new NextHumActInfoUtil().getNextHumActInfo(wfmParams);
    HumActInfoPageCtx tmpPageCtx = null;
    HumActInfoEngCtx[] nextInfo = new HumActInfoEngCtx[pageCtx.size()];
    for (int i = 0; i < pageCtx.size(); ++i) {
      HumActInfoEngCtx tmp = new HumActInfoEngCtx();
      tmpPageCtx = (HumActInfoPageCtx)pageCtx.get(i);
      tmp.setPortId(tmpPageCtx.getPortId());
      tmp.setUserPks(tmpPageCtx.getUserPks().split(","));
      nextInfo[i] = tmp;
    }
    flowInfoCtx.setNextInfo(nextInfo);
    IFlowRequest request = BizProcessServer.createFlowRequest(PwfmContext.getCurrentBpmnSession().getFormVo(), flowInfoCtx);
    IFlowResponse response = BizProcessServer.createFlowResponse();
    BizProcessServer.exe(request, response);
  }

  public void updateProdef(ProDef newprodef)
  {
    Task task = PwfmContext.getCurrentBpmnSession().getTask();
    if (task != null) {
      task.setProDef(newprodef);
      task.asyn();
    }
    ProIns proins = PwfmContext.getCurrentBpmnSession().getProIns();
    if (proins == null)
      return;
    proins.setProDef(newprodef);
    proins.asyn();

    Set<HumActIns> humactins = proins.getHumActInses();
    if ((humactins != null) && (humactins.size() > 0))
      for (HumActIns humActIns2 : humactins) {
        Set<Task> tasks = humActIns2.getTasks();
        if ((tasks != null) && (tasks.size() > 0))
          for (Task task2 : tasks) {
            task2.setProDef(newprodef);
            task2.asyn();
          }
      }
  }

  public void updateOpinion(Task task)
  {
    String opinion = PwfmContext.getCurrentBpmnSession().getOpinion();
    String oper = PwfmContext.getCurrentBpmnSession().getOperator();
    task.setOpinion(opinion);

    String operName = WfmCPTaskUtil.getDisplayName(task, WfmConstants.getDispKey(oper));

    if (WfmCPTaskUtil.isMakeBillStatus(task)) {
      operName = NCLangRes4VoTransl.getNCLangRes().getStrByID("imp", "startproins");
    }

    task.setSysext8(operName);
    task.setSysext12(oper);
  }

  public void updateAttach(Task task, String pk_subOperOpinion) {
    updateAttach(task, pk_subOperOpinion, null);
  }

  public void updateAttach(Task task, String pk_subOperOpinion, String attachState)
  {
    String sysId = PwfmContext.getCurrentBpmnSession().getSysId();
    boolean isHasAttach = false;

    ProIns proIns = PwfmContext.getCurrentBpmnSession().getProIns();
    if (StringUtils.isNotBlank(sysId)) {
      isHasAttach = updateAttach(task, pk_subOperOpinion, sysId, attachState);
    } else {
      boolean sys1 = updateAttach(task, pk_subOperOpinion, "default", attachState);
      boolean sys2 = updateAttach(task, pk_subOperOpinion, "bafile", attachState);
      isHasAttach = (sys1) || (sys2);
    }
    if (proIns != null) {
      proIns.setSysext2(UFBoolean.valueOf(isHasAttach).toString());
      proIns.asyn();
    }
  }

  public boolean updateAttach(Task task, String pk_subOperOpinion, String sysId, String attachState) {
    boolean flag = false;
    try {
      String billItem = PwfmContext.getCurrentBpmnSession().getAttachBillitem();
      if (StringUtils.isBlank(billItem)) {
        billItem = task.getProIns().getBillitem();
      }
      LfwFileVO[] attachFiles = WfmAttachUtil.getAttachFilesByBillItem(billItem, sysId);

      if ((attachFiles != null) && (attachFiles.length > 0)) {
        flag = true;
      }
      String findState = "run";
      if ("run".equals(attachState)) {
        findState = "end";
      }
      List<LfwFileVO> list = WfmAttachUtil.getRunAttachFilesByTaskPk(attachFiles, task.getPk_task(), sysId, findState);

      if ((list != null) && (list.size() != 0)) {
        if (StringUtils.isBlank(attachState)) attachState = "end";
        for (LfwFileVO lfwFileVO : list) {
          lfwFileVO.setExt3(attachState);
          lfwFileVO.setExt4(pk_subOperOpinion);
        }
        FileManager.getSystemFileManager(sysId).updataVos((LfwFileVO[])list.toArray(new LfwFileVO[0]));
      }
    }
    catch (Exception e)
    {
      WfmLogger.error(e.getMessage(), e);
    }
    return flag;
  }
}