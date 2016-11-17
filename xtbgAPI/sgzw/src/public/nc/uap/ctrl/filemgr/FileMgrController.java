package nc.uap.ctrl.filemgr;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nc.bs.framework.common.NCLocator;
import nc.uap.cpb.log.CpLogger;
import nc.uap.cpb.org.exception.CpbBusinessException;
import nc.uap.cpb.org.itf.ICpUserQry;
import nc.uap.cpb.org.util.CpbUtil;
import nc.uap.cpb.org.vos.CpUserVO;
import nc.uap.ctrl.excel.CpExcelModel;
import nc.uap.ctrl.excel.UifExcelImportUtil;
import nc.uap.ctrl.file.IFileMgrView;
import nc.uap.ctrl.filrmgr.IOccupyOperate;
import nc.uap.lfw.core.AppInteractionUtil;
import nc.uap.lfw.core.LfwRuntimeEnvironment;
import nc.uap.lfw.core.WebContext;
import nc.uap.lfw.core.WebSession;
import nc.uap.lfw.core.cmd.CmdInvoker;
import nc.uap.lfw.core.cmd.UifFileUploadCmd;
import nc.uap.lfw.core.cmd.UifPlugoutCmd;
import nc.uap.lfw.core.comp.MenuItem;
import nc.uap.lfw.core.comp.MenubarComp;
import nc.uap.lfw.core.ctrl.WindowController;
import nc.uap.lfw.core.ctx.AppLifeCycleContext;
import nc.uap.lfw.core.ctx.ApplicationContext;
import nc.uap.lfw.core.ctx.OpenProperties;
import nc.uap.lfw.core.ctx.ViewContext;
import nc.uap.lfw.core.ctx.WindowContext;
import nc.uap.lfw.core.data.Dataset;
import nc.uap.lfw.core.data.Row;
import nc.uap.lfw.core.event.DataLoadEvent;
import nc.uap.lfw.core.event.DatasetEvent;
import nc.uap.lfw.core.event.DialogEvent;
import nc.uap.lfw.core.event.MouseEvent;
import nc.uap.lfw.core.event.PageEvent;
import nc.uap.lfw.core.event.ScriptEvent;
import nc.uap.lfw.core.exception.LfwBusinessException;
import nc.uap.lfw.core.exception.LfwRuntimeException;
import nc.uap.lfw.core.model.plug.TranslatedRow;
import nc.uap.lfw.core.page.LfwView;
import nc.uap.lfw.core.page.ViewMenus;
import nc.uap.lfw.core.page.ViewModels;
import nc.uap.lfw.core.serializer.impl.SuperVO2DatasetSerializer;
import nc.uap.lfw.file.FileManager;
import nc.uap.lfw.file.LfwFileConstants;
import nc.uap.lfw.file.UploadFileHelper;
import nc.uap.lfw.file.vo.LfwFileVO;
import nc.uap.lfw.login.vo.LfwSessionBean;
import nc.uap.lfw.util.LfwClassUtil;
import nc.uap.wfm.utils.AppUtil;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import org.apache.commons.lang.StringUtils;
import uap.lfw.core.ml.LfwResBundle;
import uap.lfw.file.attach.AttachParam;
import uap.lfw.file.ncfile.NCFileManager;

public class FileMgrController
  implements WindowController, Serializable
{
  private static final long serialVersionUID = 7532916478964732880L;

  public void sysWindowClosed(PageEvent event)
  {
    LfwRuntimeEnvironment.getWebContext().destroyWebSession();
  }

  public void onAddFolderClick(MouseEvent<?> mouseEvent)
  {
    AppUtil.getCntWindowCtx().popView(new OpenProperties("newFolder", LfwResBundle.getInstance().getStrByID("imp", "FileMgrController-000004"), "502", "200"));
  }

  public void pluginmainview4folder(Map keys)
  {
    TranslatedRow r = (TranslatedRow)keys.get("pluginpara4folder");
    String foldername = (String)r.getValue("foldername");
    UFBoolean isroot = (UFBoolean)r.getValue("isroot");

    LfwView main = AppLifeCycleContext.current().getWindowContext().getViewContext("main").getView();
    Dataset masterDs = main.getViewModels().getDataset("fileds");

    Row row = masterDs.getSelectedRow();

    String parentPath = "";

    String name = foldername.trim();
    String creator = LfwRuntimeEnvironment.getLfwSessionBean().getPk_user();

    if ((row != null) && (!(isroot.booleanValue()))) {
      String isfolder = (String)row.getValue(masterDs.nameToIndex("isfolder"));
      if ((isfolder != null) && ("y".equals(isfolder)))
      {
        parentPath = (String)row.getValue(masterDs.nameToIndex("filepath"));
      }
      else
      {
        String filePath = (String)row.getValue(masterDs.nameToIndex("filepath"));
        String fileName = (String)row.getValue(masterDs.nameToIndex("filename"));
        parentPath = filePath.substring(0, filePath.indexOf(fileName) - 1);
      }
    }
    else {
      parentPath = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("billitem");
    }
    String filemgr = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("filemanager");
    String sysid = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("sysid");
    FileManager filemanager = getFileManager(filemgr, sysid);
    if (!(filemanager instanceof NCFileManager)) {
      throw new LfwRuntimeException(LfwResBundle.getInstance().getStrByID("imp", "FileMgrController-000005"));
    }
    ((NCFileManager)filemanager).newFolder(parentPath, name, creator);
    AppInteractionUtil.showShortMessage(NCLangRes4VoTransl.getNCLangRes().getStrByID("per_codes", "0per_codes0311"));
    refshData();
  }

  public void onUpload(MouseEvent<?> mouseEvent)
  {
    String title = LfwResBundle.getInstance().getStrByID("imp", "FileMgrController-000093");
    String sysid = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("sysid");
    String billitem = null;
    if ("ncfile".equals(sysid)) {
      billitem = getFolderPath();
    }
    if (billitem == null)
      billitem = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("billitem");
    String billtype = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("billtype");
    String fileext = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("fileExt");
    String filedesc = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("fileDesc");
    String sizelimit = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("sizeLimit");
    String ext1 = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("ext1");
    String ext2 = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("ext2");
    String ext3 = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("ext3");
    String ext4 = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("ext4");
    String ext5 = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("ext5");

    String bamodule = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("bamodule");

    Map map = UploadFileHelper.BuildDefaultPamater(sysid, billitem, billtype, bamodule);

    map.put("fileExt", (fileext == null) ? "" : fileext);
    map.put("fileDesc", (filedesc == null) ? "" : filedesc);
    map.put("sizeLimit", (sizelimit == null) ? "" : sizelimit);
    map.put("method", "tbcall");
    map.put("ext1", (ext1 == null) ? "" : ext1);
    map.put("ext2", (ext2 == null) ? "" : ext2);
    map.put("ext3", (ext3 == null) ? "" : ext3);
    map.put("ext4", (ext4 == null) ? "" : ext4);
    map.put("ext5", (ext5 == null) ? "" : ext5);

    UifFileUploadCmd filecmd = new UifFileUploadCmd(title, map);
    CmdInvoker.invoke(filecmd);
  }

  private String getFolderPath() {
    LfwView main = AppLifeCycleContext.current().getWindowContext().getViewContext("main").getView();
    Dataset masterDs = main.getViewModels().getDataset("fileds");

    Row row = masterDs.getSelectedRow();
    if (row == null)
      return null;
    String isfolder = (String)row.getValue(masterDs.nameToIndex("isfolder"));
    String parentPath = (String)row.getValue(masterDs.nameToIndex("filepath"));
    try {
      parentPath = URLDecoder.decode(parentPath, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new LfwRuntimeException(e);
    }

    if ((isfolder != null) && ("y".equals(isfolder)))
    {
      return parentPath;
    }

    parentPath = parentPath.substring(0, parentPath.lastIndexOf("/"));
    return parentPath;
  }

  public void onUploadedExcelFile(ScriptEvent event)
  {
    CpExcelModel model = new UifExcelImportUtil().parseExcelByEnv();
  }

  public void onDownload(MouseEvent<?> mouseEvent) {
    LfwView main = AppLifeCycleContext.current().getWindowContext().getViewContext("main").getView();
    Dataset ds = main.getViewModels().getDataset("fileds");
    String sysid = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("sysid");
    boolean isextend = true;
    if (ds != null) {
      Row row = ds.getSelectedRow();
      if (row != null) {
        isextend = false;
        String filepk = row.getString(ds.nameToIndex("id"));
        if ("ncfile".equals(sysid)) {
          String isfolder = row.getString(ds.nameToIndex("isfolder"));
          if ("y".equals(isfolder))
            throw new LfwRuntimeException(LfwResBundle.getInstance().getStrByID("imp", "FileMgrController-000006"));
          filepk = row.getString(ds.nameToIndex("filepath"));
        }
        if (filepk != null)
          CmdInvoker.invoke(new UifFileDownloadCmd(sysid, row.getString(ds.nameToIndex("filemanager")), filepk));
      }
      doExtend();
    }
    if (isextend)
      AppInteractionUtil.showMessageDialog(NCLangRes4VoTransl.getNCLangRes().getStrByID("imp", "FileMgrController-000003"));
  }

  public void onDelete(MouseEvent<?> mouseEvent)
  {
    LfwView main = AppLifeCycleContext.current().getWindowContext().getViewContext("main").getView();

    Dataset ds = main.getViewModels().getDataset("fileds");
    String sysid = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("sysid");

    boolean isextend = true;
    if (ds != null) {
      Row row = ds.getSelectedRow();
      if (row != null) {
        isextend = false;
        AppInteractionUtil.showConfirmDialog(NCLangRes4VoTransl.getNCLangRes().getStrByID("imp", "FileMgrController-000000"), NCLangRes4VoTransl.getNCLangRes().getStrByID("imp", "FileMgrController-000001"));

        if (AppInteractionUtil.getConfirmDialogResult().booleanValue()) {
          String filepk = row.getString(ds.nameToIndex("id"));
          if (filepk != null)
          {
            FileManager filemanager = getFileManager(row.getString(ds.nameToIndex("filemanager")), sysid);
            try {
              filemanager.delete(filepk);
              ds.removeRow(row);
            } catch (Exception e) {
              CpLogger.error(e);
              throw new LfwRuntimeException(e.getMessage());
            }
          }
        }
      }
      doExtend();
    }
    if (isextend) {
      AppInteractionUtil.showMessageDialog(NCLangRes4VoTransl.getNCLangRes().getStrByID("imp", "FileMgrController-000003"));
    }

    AppUtil.getCntAppCtx().addExecScript(";try{onFileDelCallback();}catch(e){}");
    initMenuState();
  }

  public void onModify(MouseEvent<?> mouseEvent)
  {
  }

  public void onScan(MouseEvent<?> mouseEvent) {
    OpenProperties winProps = new OpenProperties();
    winProps.setOpenId("pubview_scanview");
    winProps.setTitle(NCLangRes4VoTransl.getNCLangRes().getStrByID("imp", "FileMgrController-000002"));

    winProps.setWidth("577");
    winProps.setHeight("443");
    winProps.setPopclose(true);
    winProps.setButtonZone(false);
    AppLifeCycleContext.current().getApplicationContext().getCurrentWindowContext().popView(winProps);

    String sysid = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("sysid");

    String billitem = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("billitem");

    String billtype = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("billtype");

    Map paramMap = new HashMap();
    paramMap.put("url", "/pt/file/upload?");
    paramMap.put("billtype", (billtype == null) ? "" : billtype);
    paramMap.put("billitem", (billitem == null) ? "" : billitem);
    paramMap.put("sysid", (sysid == null) ? "" : sysid);
    paramMap.put("sys_datasource", "");
    paramMap.put("filemanager", "");
    new UifPlugoutCmd("main", "main_scanview_plugout", paramMap).execute();
    doExtend();
  }

  public void onView(MouseEvent<?> mouseEvent) {
    AppLifeCycleContext.current().getApplicationContext().getCurrentWindowContext().popView("view", "802", "527", NCLangRes4VoTransl.getNCLangRes().getStrByID("imp", "FileMgrController-000003"), true, false);
  }

  public void onDataLoad_fileds(DataLoadEvent dataLoadEvent)
  {
    refshData();
  }

  public void bindFiletoDS(LfwFileVO[] files, Dataset ds) throws CpbBusinessException {
    ds.clear();
    List list = new ArrayList();
    if (files != null) {
      for (LfwFileVO file : files) {
        LfwFileDsVO vo = new LfwFileDsVO();
        vo.setCreator(file.getCreator());
        vo.setId(file.getPk_lfwfile());
        vo.setName(getSimpleName(file.getDisplayname(), file.getFiletypo()));
        vo.setType(file.getFiletypo());
        if (!("y".equals(file.getIsfolder())))
          vo.setSize(getSizeStr(file.getFilesize().longValue()));
        vo.setLastmodified(file.getLastmodifytime());
        vo.setFilemanager(file.getFilemgr());
        vo.setExt1(file.getExt1());
        vo.setExt2(file.getExt2());
        vo.setExt3(file.getExt3());
        vo.setExt4(file.getExt4());
        vo.setExt5(file.getExt5());
        vo.setPk_parent(file.getPk_parent());
        vo.setPk_self(file.getPk_self());
        vo.setIsfolder(file.getIsfolder());
        vo.setFilepath(file.getFilepath());

        String pk_user = file.getLastmodifyer();
        if ((null != pk_user) && (!(pk_user.equals("")))) {
          CpUserVO user = ((ICpUserQry)NCLocator.getInstance().lookup(ICpUserQry.class)).getUserByPk(pk_user);
          if (user != null)
            vo.setLastmodifier(user.getUser_code());
          else {
            vo.setLastmodifier(pk_user);
          }
        }
        list.add(vo);
      }
    }
    new SuperVO2DatasetSerializer().serialize((SuperVO[])(SuperVO[])list.toArray(new LfwFileDsVO[0]), ds);
  }

  private String getSizeStr(long size)
  {
    String sizestr = size + "B";
    if (size <= 0L) {
      sizestr = size + "B";
    } else {
      sizestr = size + "";
      int sizelen = sizestr.length();
      if (sizelen <= 3) {
        sizestr = size + "B";
      }
      else
      {
        double newsize;
        DecimalFormat dicf;
        if ((sizelen > 3) && (sizelen <= 6)) {
          newsize = size / Math.pow(10.0D, 3.0D);
          dicf = new DecimalFormat("0.00");
          sizestr = dicf.format(newsize) + "K";
        } else if ((sizelen > 6) && (sizelen <= 9)) {
          newsize = size / Math.pow(10.0D, 6.0D);
          dicf = new DecimalFormat("0.00");
          sizestr = dicf.format(newsize) + "M";
        } else {
          newsize = size / Math.pow(10.0D, 9.0D);
          dicf = new DecimalFormat("0.00");
          sizestr = dicf.format(newsize) + "G"; }
      }
    }
    return sizestr;
  }

  private String getSimpleName(String filename, String type) {
    if ((null == filename) || (filename.equals("")))
      return "";
    if ((null == type) || (type.equals("")))
      return filename;
    if (type.equals("NaN")) {
      return filename;
    }
    String simplename = "";
    Pattern pattern = Pattern.compile("\\." + type + "$");
    simplename = pattern.matcher(filename).replaceAll("");

    return simplename;
  }

  public void refshDataByScript(ScriptEvent event) {
    refshData();
    doExtend();
  }

  public void refshData() {
    String sysid = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("sysid");
    String billitem = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("billitem");
    String billtype = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("billtype");
    String filemgr = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("filemanager");
    FileManager filemanager = getFileManager(filemgr, sysid);
    try {
      LfwFileVO[] files = filemanager.getAttachFileByItemID(billitem, billtype);
      files = afterLoad(files);
      Dataset ds = AppLifeCycleContext.current().getWindowContext().getViewContext("main").getView().getViewModels().getDataset("fileds");
      ds.clear();
      bindFiletoDS(files, ds);
      if ("ncfile".equals(sysid))
        AppUtil.getCntAppCtx().addExecScript("GridComp.expandAllNodes('filegrid')");
    } catch (LfwBusinessException e) {
      CpLogger.error(e);
      throw new LfwRuntimeException(e.getMessage());
    }
  }

  public void changeFileName(ScriptEvent event) {
    String pk_file = AppLifeCycleContext.current().getParameter("pk_file");
    String newname = AppLifeCycleContext.current().getParameter("newname");
    String filemanager = AppLifeCycleContext.current().getParameter("filemanager");
    String sysid = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("sysid");

    FileManager fileManager = (FileManager)LfwClassUtil.newInstance(filemanager);
    try {
      Dataset ds = AppLifeCycleContext.current().getWindowContext().getViewContext("main").getView().getViewModels().getDataset("fileds");
      Row row = ds.getSelectedRow();
      if ("ncfile".equals(sysid))
        pk_file = (String)row.getValue(ds.nameToIndex("filepath"));
      LfwFileVO filevo = fileManager.getFileVO(pk_file);
      if (null != filevo) {
        if (fileManager instanceof NCFileManager) {
          String filepath = (String)row.getValue(ds.nameToIndex("filepath"));
          String type = (String)row.getValue(ds.nameToIndex("type"));
          String newpath = ((NCFileManager)fileManager).rename(filepath, newname + "." + type);
          String isfolder = (String)row.getValue(ds.nameToIndex("isfolder"));
          if ((isfolder != null) && ("y".equals(isfolder))) {
            refshData();
          }
          else {
            row.setValue(ds.nameToIndex("id"), newpath);
            row.setValue(ds.nameToIndex("filepath"), newpath);
          }
        }
        else {
          filevo.setDisplayname(newname + "." + filevo.getFiletypo());
          filevo.setLastmodifytime(new UFDateTime());
          filevo.setLastmodifyer(CpbUtil.getCntUserCode());
          fileManager.updateVo(filevo);
        }
        row.setValue(ds.nameToIndex("name"), newname);
        row.setValue(ds.nameToIndex("lastmodified"), filevo.getLastmodifytime());
        row.setValue(ds.nameToIndex("lastmodifier"), filevo.getLastmodifyer());
      }
    } catch (Exception e) {
      CpLogger.error(e);
      throw new LfwRuntimeException(e.getMessage());
    }
    doExtend();
    AppUtil.getCntAppCtx().addExecScript(";try{onFileNameChangeCallback();}catch(e){}");
  }

  public void btnokonclick(MouseEvent mouseEvent) {
    AppLifeCycleContext.current().getApplicationContext().closeWinDialog();
  }

  public void btncancelonclick(MouseEvent mouseEvent) {
    AppLifeCycleContext.current().getApplicationContext().closeWinDialog();
  }

  public void onbeforeShow(DialogEvent dialogEvent) {
    initMenuState();
  }

  public void pluginmain_plugin(Map keys) {
    refshData();
  }

  public void onAfterRowSelect(DatasetEvent se) {
    initMenuState();
  }

  private void initMenuState()
  {
    LfwView main = AppLifeCycleContext.current().getWindowContext().getViewContext("main").getView();

    MenubarComp menubar = main.getViewMenus().getMenuBar("opemenu");
    Dataset ds = main.getViewModels().getDataset("fileds");
    Row selectedRow = ds.getSelectedRow();
    String state = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("state");

    String printable = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter(AttachParam.ALLOWATTACHPRINT);

    String editonlieable = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter(AttachParam.ALLOWATTACHEDITONLINE);

    String isReadState = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter(AttachParam.ISREADSTATE);

    AppLifeCycleContext.current().getApplicationContext().setClientAttribute("editonlieable", returnCast(editonlieable));

    AppLifeCycleContext.current().getApplicationContext().setClientAttribute("printable", returnCast(printable));

    AttachParam attachParam = getInitAttachParam(selectedRow);

    if (menubar.getItem("scan") != null) {
      menubar.getItem("scan").setVisible(false);
    }
    LfwFileVO lfwFileVO = null;
    if (selectedRow != null) {
      lfwFileVO = new LfwFileVO();
      String creator = selectedRow.getString(ds.nameToIndex("creator"));
      String ext1 = selectedRow.getString(ds.nameToIndex("ext1"));
      String ext2 = selectedRow.getString(ds.nameToIndex("ext2"));
      String ext3 = selectedRow.getString(ds.nameToIndex("ext3"));
      lfwFileVO.setCreator(creator);
      lfwFileVO.setExt1(ext1);
      lfwFileVO.setExt2(ext2);
      lfwFileVO.setExt3(ext3);
      String sysid = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("sysid");

      String filepk = selectedRow.getString(ds.nameToIndex("id"));
      if (filepk != null)
      {
        FileManager filemanager = getFileManager(selectedRow.getString(ds.nameToIndex("filemanager")), sysid);
        try {
          lfwFileVO = filemanager.getFileVO(filepk);
        } catch (Exception e) {
          CpLogger.error(e);
        }
      }
    }
    if (StringUtils.isNotBlank(getControlclzz())) {
      IFileMgrView fileMgrView = (IFileMgrView)LfwClassUtil.newInstance(getControlclzz());

      attachParam = fileMgrView.initAttachParam(attachParam, lfwFileVO);
    }
    if (StringUtils.isNotBlank(getControlclzzes())) {
      String[] strs = getControlclzzes().split(",");
      for (String ctrlClazz : strs) {
        IFileMgrView fileMgrView = (IFileMgrView)LfwClassUtil.newInstance(ctrlClazz);
        attachParam = fileMgrView.initAttachParam(attachParam, lfwFileVO);
      }
    }

    initMenuSateByAttachParam(attachParam);
    String sysid = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("sysid");
    if ("ncfile".equals(sysid))
      menubar.getItem("newfolder").setVisible(true); 
  }

  private AttachParam getInitAttachParam(Row selectedRow) {
    AttachParam attachParam = new AttachParam();
    String state = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("state");

    if (state != null) {
      int cstate = -1;
      try {
        if ((state != null) && (!("".equals(state))))
          cstate = Integer.parseInt(state);
        else
          cstate = -1;
      } catch (Exception e) {
        CpLogger.error(e.getMessage(), e);
        cstate = 0;
      }

      if ((-1 == cstate) && (selectedRow != null)) {
        attachParam.setAllowAttachDelete(UFBoolean.TRUE);
        attachParam.setAllowAttachDownload(UFBoolean.TRUE);
        attachParam.setAllowAttachEdit(UFBoolean.TRUE);
        attachParam.setAllowAttachLook(UFBoolean.TRUE);
        attachParam.setAllowAttachUpload(UFBoolean.TRUE);
      }
      if (0 == cstate) {
        attachParam.setAllowAttachDelete(UFBoolean.FALSE);
        attachParam.setAllowAttachDownload(UFBoolean.FALSE);
        attachParam.setAllowAttachEdit(UFBoolean.FALSE);
        attachParam.setAllowAttachLook(UFBoolean.FALSE);
        attachParam.setAllowAttachUpload(UFBoolean.FALSE);
      }
      else {
        attachParam.setAllowAttachUpload(UFBoolean.valueOf((0x10 & cstate) == 16));
        attachParam.setAllowAttachDownload(UFBoolean.valueOf(((0x8 & cstate) == 8) && (selectedRow != null)));

        attachParam.setAllowAttachDelete(UFBoolean.valueOf(((0x4 & cstate) == 4) && (selectedRow != null)));

        attachParam.setAllowAttachEdit(UFBoolean.valueOf(((0x2 & cstate) == 2) && (selectedRow != null)));
        attachParam.setAllowAttachLook(UFBoolean.valueOf(((0x1 & cstate) == 1) && (selectedRow != null)));
      }
    }
    else {
      String uploadValue = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter(AttachParam.ALLOWATTACHUPLOAD);

      if (!("-1".equals(uploadValue))) {
        attachParam.setAllowAttachUpload(UFBoolean.valueOf(!("0".equals(uploadValue))));
      }
      String deleteValue = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter(AttachParam.ALLOWATTACHDELETE);

      if (!("-1".equals(deleteValue))) {
        attachParam.setAllowAttachDelete(UFBoolean.valueOf(!("0".equals(deleteValue))));
      }

      String downLoadValue = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter(AttachParam.ALLOWATTACHDOWNLOAD);

      if (!("-1".equals(downLoadValue))) {
        attachParam.setAllowAttachDownload(UFBoolean.valueOf(!("0".equals(downLoadValue))));
      }

      String editValue = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter(AttachParam.ALLOWATTACHEDIT);

      if (!("-1".equals(editValue))) {
        attachParam.setAllowAttachEdit(UFBoolean.valueOf(!("0".equals(editValue))));
      }

      String editOnlineValue = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter(AttachParam.ALLOWATTACHEDITONLINE);

      if (!("-1".equals(editOnlineValue))) {
        attachParam.setAllowAttachEditOnline(UFBoolean.valueOf(!("0".equals(editOnlineValue))));
      }

      String lookValue = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter(AttachParam.ALLOWATTACHLOOK);

      if (!("-1".equals(lookValue))) {
        attachParam.setAllowAttachLook(UFBoolean.valueOf(!("0".equals(lookValue))));
      }

      String printVlaue = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter(AttachParam.ALLOWATTACHPRINT);

      if (!("-1".equals(printVlaue))) {
        attachParam.setAllowAttachPrint(UFBoolean.valueOf(!("0".equals(printVlaue))));
      }

    }

    if (selectedRow == null) {
      attachParam.setAllowAttachDownload(UFBoolean.FALSE);
      attachParam.setAllowAttachDelete(UFBoolean.FALSE);
      attachParam.setAllowAttachEdit(UFBoolean.FALSE);
      attachParam.setAllowAttachLook(UFBoolean.FALSE);
    }
    return attachParam; }

  private void initMenuSateByAttachParam(AttachParam attachParam) {
    LfwView main = AppLifeCycleContext.current().getWindowContext().getViewContext("main").getView();

    MenubarComp menubar = main.getViewMenus().getMenuBar("opemenu");
    if (attachParam.getAllowAttachUpload() != null) {
      menubar.getItem("upload").setEnabled(attachParam.getAllowAttachUpload().booleanValue());
    }

    if (attachParam.getAllowAttachDelete() != null) {
      menubar.getItem("btn_delete").setEnabled(attachParam.getAllowAttachDelete().booleanValue());
    }
    if (attachParam.getAllowAttachDownload() != null) {
      menubar.getItem("download").setEnabled(attachParam.getAllowAttachDownload().booleanValue());
    }
    if (attachParam.getAllowAttachEdit() != null) {
      menubar.getItem("modify").setEnabled(attachParam.getAllowAttachEdit().booleanValue());
    }
    if (attachParam.getAllowAttachLook() != null) {
      menubar.getItem("view").setEnabled(attachParam.getAllowAttachLook().booleanValue());
    }
    if (attachParam.getAllowAttachEditOnline() != null) {
      AppLifeCycleContext.current().getApplicationContext().setClientAttribute("editonlieable", (attachParam.getAllowAttachEditOnline().booleanValue()) ? "true" : "false");
    }

    if (attachParam.getAllowAttachPrint() != null)
      AppLifeCycleContext.current().getApplicationContext().setClientAttribute("printable", (attachParam.getAllowAttachPrint().booleanValue()) ? "true" : "false");
  }

  private LfwFileVO[] afterLoad(LfwFileVO[] lfwFileVOs)
  {
    if (StringUtils.isNotBlank(getControlclzz())) {
      IFileMgrView fileMgrView = (IFileMgrView)LfwClassUtil.newInstance(getControlclzz());
      lfwFileVOs = fileMgrView.afterLoad(lfwFileVOs);
    }
    if (StringUtils.isNotBlank(getControlclzzes())) {
      String[] strs = getControlclzzes().split(",");
      for (String ctrlClazz : strs) {
        IFileMgrView fileMgrView = (IFileMgrView)LfwClassUtil.newInstance(ctrlClazz);
        lfwFileVOs = fileMgrView.afterLoad(lfwFileVOs);
      }
    }
    return lfwFileVOs; }

  private String getControlclzz() {
    String controlclzz = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("IFilemMgrViewClazz");

    return controlclzz;
  }

  private String getControlclzzes()
  {
    String controlclzz = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter("IFilemMgrViewClazzes");

    if (StringUtils.isBlank(controlclzz)) {
      controlclzz = (String)AppUtil.getAppAttr("IFilemMgrViewClazzes");
    }
    return controlclzz;
  }

  private void doExtend() {
    String controlclzz = LfwRuntimeEnvironment.getWebContext().getWebSession().getOriginalParameter(LfwFileConstants.Filemgr_Para_OperateClazz);

    IOccupyOperate op = null;
    if ((controlclzz == null) || ("".equals(controlclzz)))
      return;
    try {
      op = (IOccupyOperate)Class.forName(controlclzz).newInstance();
    } catch (InstantiationException e) {
      CpLogger.error(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      CpLogger.error(e.getMessage(), e);
    } catch (ClassNotFoundException e) {
      CpLogger.error(e.getMessage(), e);
    }
    if (op != null)
      op.handleWfmInfo();
  }

  private FileManager getFileManager(String filemanager, String sysid)
  {
    FileManager fileManager = null;
    if (StringUtils.isNotBlank(filemanager)) {
      fileManager = (FileManager)LfwClassUtil.newInstance(filemanager);
    }
    else if (StringUtils.isNotBlank(sysid))
      fileManager = FileManager.getSystemFileManager(sysid);
    else
      return FileManager.getSystemFileManager("bafile");
    return fileManager;
  }

  private String returnCast(String value) {
    String booleanStr = "false";
    if (value == null) return booleanStr;

    if (value.equals("1")) {
      booleanStr = "true";
    }
    else if (value.equals("0")) {
      booleanStr = "false";
    }
    else if (value.equals("-1")) {
      booleanStr = "false";
    }
    return booleanStr;
  }
}