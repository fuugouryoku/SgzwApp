package nc.uap.wfm.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nc.bs.framework.common.NCLocator;
import nc.uap.lfw.core.ctx.AppLifeCycleContext;
import nc.uap.lfw.core.ctx.ApplicationContext;
import nc.uap.lfw.core.file.FillFileInfoHelper;
import nc.uap.lfw.file.FileManager;
import nc.uap.lfw.file.vo.LfwFileVO;
import nc.uap.wfm.itf.IWfmTaskQry;
import nc.uap.wfm.logger.WfmLogger;
import nc.uap.wfm.vo.WfmTaskVO;
import org.apache.commons.lang.StringUtils;
import uap.web.bd.pub.AppUtil;

public class WfmAttachUtil
{
  public static final String ATTACH_STATE_RUN = "run";
  public static final String ATTACH_STATE_END = "end";

  public static void addBillItemIntoSession(String billItem)
  {
    AppUtil.addAppAttr("WfmAppAttr_FormInFoCtx_Billitem", billItem);
  }

  public static void resetBillItem() {
    AppLifeCycleContext.current().getApplicationContext().addAppAttribute("WfmAppAttr_FormInFoCtx_Billitem", null);
    addBillItemIntoSession(FillFileInfoHelper.getOrCreateItem());
  }

  public static String getBillItem() {
    boolean isNc = WfmBillUtil.isNCBill();
    String billitem = (String)AppUtil.getAppAttr("WfmAppAttr_FormInFoCtx_Billitem");
    if (isNc)
    {
      if (StringUtils.isBlank(billitem)) {
        return FillFileInfoHelper.getOrCreateItem();
      }
      return billitem;
    }

    String taskPk = getTaskPkFromSession();
    if (StringUtils.isNotBlank(taskPk)) {
      String tbillitem = WfmTaskUtil.getBillItemByTaskpk(taskPk);
      if ((StringUtils.isBlank(billitem)) || ((StringUtils.isNotBlank(tbillitem)) && (!(tbillitem.equals(billitem))))) {
        return tbillitem;
      }
    }
    else if (StringUtils.isBlank(billitem)) {
      return FillFileInfoHelper.getOrCreateItem();
    }

    return billitem; }

  private static String getTaskPkFromSession() {
    if (!(AppUtil.isApp())) return null;
    Object obj = AppUtil.getAppAttr("$$$$$$$$TaskPk");
    if (obj == null) return null;
    String taskPk = (String)obj;
    if ("null".equalsIgnoreCase(taskPk)) return null;
    return taskPk;
  }

  public static LfwFileVO[] getAttachFilesByBillItem(String billItem, String sysId)
  {
    if (StringUtils.isBlank(billItem)) return null;
    LfwFileVO[] attachFiles = null;
    try {
      attachFiles = FileManager.getSystemFileManager(sysId).getAttachFileByItemID(billItem, "wfm_attach");
    } catch (Exception e) {
      WfmLogger.error(e.getMessage(), e);
    }
    return attachFiles;
  }

  public static LfwFileVO[] filteRuningAttaches(LfwFileVO[] lfwFileVOs, String pk_task, boolean isOnlyEnd)
  {
    if ((lfwFileVOs == null) || (lfwFileVOs.length == 0)) return null;
    if (StringUtils.isBlank(pk_task)) {
      pk_task = WfmTaskUtil.getTaskPkFromSession();
    }
    if (StringUtils.isBlank(pk_task)) return lfwFileVOs;
    List resultList = new ArrayList();
    Set taskPkSet = new HashSet();
    StringBuilder buf = new StringBuilder();
    List<LfwFileVO> notDoList = new ArrayList();
    for (LfwFileVO lfwFileVO : lfwFileVOs) {
      String pk_tempTask = lfwFileVO.getExt1();
      if ((StringUtils.isBlank(pk_tempTask)) || ("null".equals(pk_tempTask))) {
        resultList.add(lfwFileVO);
      } else {
        if (("run".equals(lfwFileVO.getExt3())) && (!(pk_tempTask.equals(pk_task)))) {
          continue;
        }
        if (taskPkSet.add(pk_tempTask)) {
          buf.append(",'").append(pk_tempTask).append("'");
        }
        notDoList.add(lfwFileVO);
      }
    }
    if (buf.length() > 0) {
      Map taskVoMap;
      String where = new StringBuilder().append("pk_task in (").append(buf.substring(1)).append(")").toString();
      try {
        WfmTaskVO[] taskVos = ((IWfmTaskQry)NCLocator.getInstance().lookup(IWfmTaskQry.class)).getTaskVOsByWhere(where);
        if ((taskVos != null) && (taskVos.length > 0)) {
          taskVoMap = new HashMap();
          for (WfmTaskVO tempTask : taskVos) {
            taskVoMap.put(tempTask.getPk_task(), tempTask);
          }
          for (LfwFileVO lfwFileVO : notDoList)
          {
            WfmTaskVO tempTask = (WfmTaskVO)taskVoMap.get(lfwFileVO.getExt1());

            if ((tempTask != null) && ((("State_Run".equalsIgnoreCase(tempTask.getState())) || ("State_Plmnt".equalsIgnoreCase(tempTask.getState()))))) {
              if (lfwFileVO.getExt1().equals(pk_task)) {
                resultList.add(lfwFileVO);
              }
            }
            else if (isOnlyEnd) {
              if ("end".equals(lfwFileVO.getExt3()))
                resultList.add(lfwFileVO);
            }
            else
              resultList.add(lfwFileVO);
          }
        }
      }
      catch (Exception e)
      {
        WfmLogger.error(e);
      }
    }
    return ((LfwFileVO[])resultList.toArray(new LfwFileVO[0]));
  }

  public static List<LfwFileVO> getRunAttachFilesByTaskPk(LfwFileVO[] attachFiles, String pk_taskTemp, String sysId, String attachState)
  {
    List resultList = new ArrayList();
    List<LfwFileVO> list = getAttachFiles(attachFiles, null, sysId, pk_taskTemp, null);
    if (StringUtils.isBlank(attachState)) attachState = "run";
    if ((list == null) || (list.size() == 0)) return null;
    for (LfwFileVO lfwFileVO : list) {
      if ((attachState.equals(lfwFileVO.getExt3())) || ((StringUtils.isBlank(lfwFileVO.getExt3())) && ("run".equals(attachState)))) {
        resultList.add(lfwFileVO);
      }
    }
    return resultList;
  }

  public static List<LfwFileVO> getRunAttachFilesByTaskPk(LfwFileVO[] attachFiles, String pk_taskTemp, String sysId)
  {
    List resultList = new ArrayList();
    List<LfwFileVO> list = getAttachFiles(attachFiles, null, sysId, pk_taskTemp, null);
    if ((list == null) || (list.size() == 0)) return null;
    for (LfwFileVO lfwFileVO : list) {
      if ("run".equals(lfwFileVO.getExt3())) {
        resultList.add(lfwFileVO);
      }
    }
    return resultList;
  }

  public static List<LfwFileVO> getAttachFilesByTaskPk(LfwFileVO[] attachFiles, String pk_taskTemp, String sysId)
  {
    return getAttachFiles(attachFiles, null, sysId, pk_taskTemp, null);
  }

  public static List<LfwFileVO> getAttachFiles(LfwFileVO[] attachFiles, String billItem, String sysId, String pk_taskTemp, String pk_startTask)
  {
    if (StringUtils.isBlank(pk_taskTemp)) return null;
    Object proins = null;
    if (StringUtils.isBlank(pk_startTask)) {
      proins = WfmTaskUtil.getProInsByTask(WfmTaskUtil.getBasicTaskFromDB(pk_taskTemp));
      pk_startTask = WfmProinsUtil.getStartTaskPk(proins);
    }

    if (StringUtils.isBlank(billItem)) {
      if (proins == null)
        proins = WfmTaskUtil.getProInsByTask(WfmTaskUtil.getBasicTaskFromDB(pk_taskTemp));
      billItem = WfmProinsUtil.getBillitem(proins);
    }
    if (attachFiles == null) {
      attachFiles = getAttachFilesByBillItem(billItem, sysId);
    }
    List resultList = new ArrayList();
    if ((attachFiles != null) && (attachFiles.length > 0)) {
      for (LfwFileVO lfwFileVO : attachFiles) {
        if (pk_taskTemp.equals(pk_startTask)) {
          if ((getAttachAtSartTask(attachFiles, pk_startTask) != null) && (getAttachAtSartTask(attachFiles, pk_startTask).contains(lfwFileVO))) {
            resultList.add(lfwFileVO);
          }
        }
        else if (pk_taskTemp.equalsIgnoreCase(lfwFileVO.getExt1())) {
          resultList.add(lfwFileVO);
        }
      }
    }

    return resultList; }

  public static List<LfwFileVO> getAttachAtSartTask(LfwFileVO[] attachFiles, String pk_startTask) {
    List resultList = new ArrayList();
    if ((attachFiles != null) && (attachFiles.length > 0)) {
      for (LfwFileVO lfwFileVO : attachFiles)
      {
        if ((lfwFileVO.getExt1() == null) || ("".equalsIgnoreCase(lfwFileVO.getExt1())) || ("null".equalsIgnoreCase(lfwFileVO.getExt1())) || (pk_startTask.equals(lfwFileVO.getExt1()))) {
          resultList.add(lfwFileVO);
        }
      }
    }
    return resultList;
  }
}