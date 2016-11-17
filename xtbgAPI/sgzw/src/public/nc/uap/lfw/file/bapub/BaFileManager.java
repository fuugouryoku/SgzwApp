package nc.uap.lfw.file.bapub;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nc.uap.lfw.core.LfwRuntimeEnvironment;
import nc.uap.lfw.core.exception.LfwBusinessException;
import nc.uap.lfw.core.exception.LfwRuntimeException;
import nc.uap.lfw.core.log.LfwLogger;
import nc.uap.lfw.file.vo.LfwFileVO;
import nc.uap.portal.comm.file.PortalFileManager;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import uap.pub.fs.client.FileStorageClient;
import uap.pub.fs.client.IFileTransfer;
import uap.pub.fs.domain.basic.FileHeader;
import uap.pub.fs.domain.basic.FileHeaderCombineModel;
import uap.pub.fs.domain.ext.BaFileStoreExt;
import uap.pub.fs.domain.ext.IFileStorageExt;
import uap.pub.fs.exception.FileStorageException;

public class BaFileManager extends PortalFileManager
{
  private FileStorageClient getFileStorageClient(String moduleid)
    throws LfwBusinessException
  {
    FileStorageClient client = FileStorageClient.getInstance();
    if (client == null) {
      throw new LfwBusinessException("[BaFileManager Error] can not load FileStorageClient insatnce");
    }
    client.setbucket(moduleid);
    return client;
  }

  public String upload(String fileName, String billType, String billItem, long size, InputStream in)
    throws Exception
  {
    return upload(fileName, billType, billItem, size, in, false);
  }

  public String upload(String fileName, String billType, String billItem, long size, InputStream in, boolean override)
    throws Exception
  {
    return upload(fileName, billType, billItem, size, in, override, null, null, null, null, null);
  }

  public String upload(String fileName, String billType, String billItem, long size, InputStream in, boolean override, String ext1, String ext2, String ext3, String ext4, String ext5)
    throws Exception
  {
    BaFileStoreExt ext = new BaFileStoreExt();
    ext.setPk_billitem(billItem);
    ext.setPk_billtype(billType);
    ext.setExt1(ext1);
    ext.setExt2(ext2);
    ext.setExt3(ext3);
    ext.setExt4(ext4);
    ext.setExt5(ext5);
    FileHeader Header = getFileStorageClient(getBamodule()).uploadFile(fileName, in, override, new IFileStorageExt[] { ext });

    return Header.getPath();
  }

  public void upload(LfwFileVO vo, InputStream in) throws Exception
  {
    if (vo.getFilemgr().equals(BaFileManager.class.getName())) {
      BaFileStoreExt ext = new BaFileStoreExt();
      ext.setPk_billitem(vo.getPk_billitem());
      ext.setPk_billtype(vo.getPk_billtype());
      ext.setExt1(vo.getExt1());
      ext.setExt2(vo.getExt2());
      ext.setExt3(vo.getExt3());
      ext.setExt4(vo.getExt4());
      ext.setExt5(vo.getExt5());

      FileHeader Header = getFileStorageClient(getBamodule()).uploadFile(vo.getFilename(), in, false, new IFileStorageExt[] { ext });

      vo.setPk_lfwfile(Header.getPath());
    } else {
      super.upload(vo, in);
    }
  }

  public void ReUpload(String filepk, long size, InputStream in)
    throws Exception
  {
    getFileStorageClient(getTrueBaModule(filepk)).updateFile(filepk, in);
  }

  public void ReUpload(LfwFileVO vo, InputStream in)
    throws Exception
  {
    if (vo.getFilemgr().equals(BaFileManager.class.getName()))
    {
      ReUpload(vo.getPk_lfwfile(), 0L, in);

      BaFileStoreExt ext = new BaFileStoreExt();
      ext.setPk_billitem(vo.getPk_billitem());
      ext.setPk_billtype(vo.getPk_billtype());
      ext.setExt1(vo.getExt1());
      ext.setExt2(vo.getExt2());
      ext.setExt3(vo.getExt3());
      ext.setExt4(vo.getExt4());
      ext.setExt5(vo.getExt5());

      getFileStorageClient(getTrueBaModule(vo.getPk_lfwfile())).updateExtProps(vo.getPk_lfwfile(), new IFileStorageExt[] { ext });
    }
    else {
      super.ReUpload(vo, in);
    }
  }

  public String copyFile(String fileName, String billType, String billItem, String category, String oldFilePK)
    throws Exception
  {
    return copyFile(fileName, billType, billItem, category, oldFilePK, getBamodule());
  }

  public String copyFile(String fileName, String billType, String billItem, String category, String oldFilePK, String newbamodule)
    throws Exception
  {
    IFileTransfer fileTans = FileStorageClient.getInstance().getFileTransfer();

    BaFileStoreExt ext = new BaFileStoreExt();
    ext.setPk_billitem(billItem);
    ext.setPk_billtype(billType);

    FileHeader Header = fileTans.copy(oldFilePK, newbamodule, fileName, new IFileStorageExt[] { ext });

    return Header.getPath();
  }

  public LfwFileVO getFileVO(String filePK) throws LfwBusinessException
  {
    LfwFileVO[] vos = getFileVO(new String[] { filePK });
    if ((vos != null) && (vos.length > 0)) {
      return vos[0];
    }
    return super.getFileVO(filePK);
  }

  private LfwFileVO[] getFileVO(String[] filePKs)
    throws LfwBusinessException
  {
    FileHeaderCombineModel[] filevos = FileStorageClient.getInstance().getFileTransfer().queryHeaderCombModel(filePKs);
    if ((filevos == null) || (filevos.length < 0)) {
      return null;
    }
    List vos = new ArrayList();
    for (int i = 0; i < filevos.length; ++i) {
      BaFileStoreExt ext = null;
      IFileStorageExt[] exts = filevos[i].getExtVos();
      if ((exts != null) && (exts.length > 0)) {
        ext = (BaFileStoreExt)exts[0];
      }
      LfwFileVO vo = convertToLfwFileVo(filevos[i].getFileHeader(), ext);
      vos.add(vo);
    }
    return ((LfwFileVO[])vos.toArray(new LfwFileVO[0]));
  }

  private LfwFileVO convertToLfwFileVo(FileHeader header, BaFileStoreExt ext) {
    LfwFileVO vo = new LfwFileVO();
    vo.setPk_lfwfile(header.getPath());
    vo.setCreator(header.getCreator());
    vo.setLastmodifyer(header.getLastModifier());
    vo.setCreattime(header.getCreatetime());
    vo.setLastmodifytime(header.getLastModifytime());

    vo.setFilename(header.getName());
    vo.setDisplayname(header.getName());
    vo.setFiletypo(header.getFileType());
    vo.setFilesize(header.getFileSize());
    vo.setTs((header.getLastModifytime() == null) ? header.getCreatetime() : header.getLastModifytime());

    if (ext != null) {
      vo.setPk_group(ext.getPk_group());
      vo.setPk_billitem(ext.getPk_billitem());
      vo.setPk_billtype(ext.getPk_billtype());
      vo.setCreatestatus(ext.getCreatestatus());
      vo.setExt1(ext.getExt1());
      vo.setExt2(ext.getExt2());
      vo.setExt3(ext.getExt3());
      vo.setExt4(ext.getExt4());
      vo.setExt5(ext.getExt5());
    }
    vo.setFilemgr(BaFileManager.class.getName());
    return vo;
  }

  public String insertFileVO(LfwFileVO vo)
    throws LfwBusinessException
  {
    try
    {
      upload(vo, null);
    } catch (Exception e) {
      LfwLogger.error(e);
      throw new LfwBusinessException(e);
    }
    return vo.getPk_lfwfile();
  }

  public void delete(String fileNo)
    throws Exception
  {
    FileStorageClient.getInstance().getFileTransfer(getTrueBaModule(fileNo)).remove(fileNo);

    if (fileNo != null)
      super.delete(fileNo);
  }

  public void delete(LfwFileVO vo)
    throws Exception
  {
    if (vo == null)
      return;
    delete(vo.getPk_lfwfile());
  }

  public void updateVo(LfwFileVO vo)
    throws LfwBusinessException
  {
    updataVos(new LfwFileVO[] { vo });
  }

  public void updataVos(LfwFileVO[] vos)
    throws LfwBusinessException
  {
    if ((vos == null) || (vos.length < 1)) {
      return;
    }
    List bavolist = new ArrayList();
    List supevolist = new ArrayList();
    for (LfwFileVO vo : vos) {
      if (vo.getFilemgr().equals(BaFileManager.class.getName()))
        bavolist.add(vo);
      else
        supevolist.add(vo);
    }
    if (supevolist.size() > 0) {
      LfwFileVO[] supervos = (LfwFileVO[])supevolist.toArray(new LfwFileVO[0]);
      super.updataVos(supervos);
    }
    if (bavolist.size() < 1)
      return;
    LfwFileVO[] bavos = (LfwFileVO[])bavolist.toArray(new LfwFileVO[0]);

    if ((bavos == null) || (bavos.length < 1)) {
      return;
    }
    String[] pks = new String[bavos.length];

    for (int i = 0; i < pks.length; ++i) {
      pks[i] = bavos[i].getPk_lfwfile();
    }

    FileHeaderCombineModel[] filevos = FileStorageClient.getInstance().getFileTransfer().queryHeaderCombModel(pks);

    Map map = new HashMap();
    Map headerMap = new HashMap();
    if (filevos != null) {
      for (int i = 0; i < filevos.length; ++i) {
        FileHeaderCombineModel ext = filevos[i];
        headerMap.put(ext.getFileHeader().getPath(), ext.getFileHeader());

        if (ext != null) {
          map.put(ext.getFileHeader().getPath(), (BaFileStoreExt)(BaFileStoreExt)(((ext.getExtVos() == null) || (ext.getExtVos().length < 1)) ? null : ext.getExtVos()[0]));
        }

      }

    }

    for (int i = 0; i < bavos.length; ++i) {
      LfwFileVO vo = bavos[i];

      FileHeader header = (FileHeader)headerMap.get(vo.getPk_lfwfile());
      header.setName(vo.getDisplayname());
      BaFileStoreExt ext = (BaFileStoreExt)map.get(vo.getPk_lfwfile());
      if (ext == null)
        ext = new BaFileStoreExt();
      ext.setPk_billtype(vo.getPk_billtype());
      ext.setPk_billitem(vo.getPk_billitem());
      ext.setPk_group(vo.getPk_group());
      ext.setCategory("");
      ext.setCreatestatus(vo.getCreatestatus());
      ext.setExt1(vo.getExt1());
      ext.setExt2(vo.getExt2());
      ext.setExt3(vo.getExt3());
      ext.setExt4(vo.getExt4());
      ext.setExt5(vo.getExt5());
      FileStorageClient.getInstance().getFileTransfer().update(header, null, new IFileStorageExt[] { ext });
    }
  }

  public void download(String fileNo, OutputStream out)
    throws Exception
  {
    LfwFileVO vo = getFileVO(fileNo);
    if (vo == null)
      return;
    if (vo.getFilemgr().equals(BaFileManager.class.getName())) {
      FileStorageClient.getInstance().getFileTransfer(getTrueBaModule(fileNo)).download(fileNo, out);
    }
    else
      super.download(fileNo, out);
  }

  public boolean exist(String fileNo)
    throws Exception
  {
    LfwFileVO vo = getFileVO(fileNo);

    return (vo == null);
  }

  public boolean existInFs(String fileNo)
    throws Exception
  {
    return ((FileStorageClient.getInstance().getFileTransfer(getTrueBaModule(fileNo)).exist(fileNo)) || (super.existInFs(fileNo)));
  }

  public void billSaveComplete(String billitem)
    throws LfwRuntimeException
  {
    try
    {
      LfwFileVO[] vos = getAttachFileByItemID(billitem);

      if ((vos == null) || (vos.length < 1))
        return;
      for (int i = 0; i < vos.length; ++i)
        vos[i].setCreatestatus("1");
      updataVos(vos);
    } catch (LfwBusinessException e) {
      LfwLogger.error(e);
      throw new LfwRuntimeException(e);
    }
  }

  public String getFileType(String fileName)
  {
    return super.getFileType(fileName);
  }

  public LfwFileVO[] getAttachFileByItemID(String itempk)
    throws LfwBusinessException
  {
    return getAttachFileByItemID(itempk, null);
  }

  public LfwFileVO[] getAttachFileByItemID(String itempk, String billtype)
    throws LfwBusinessException
  {
    if (StringUtils.isEmpty(itempk))
      return null;
    BaFileStoreExt ext = new BaFileStoreExt();
    ext.setPk_billitem(itempk);
    if (!(StringUtils.isEmpty(billtype))) {
      ext.setPk_billtype(billtype);
    }
    FileHeaderCombineModel[] headers = FileStorageClient.getInstance().getFileTransfer().queryHeaderCombModel(new IFileStorageExt[] { ext });

    LfwFileVO[] vos = new LfwFileVO[headers.length];
    for (int i = 0; i < headers.length; ++i) {
      FileHeader header = headers[i].getFileHeader();
      if (header == null)
        continue;
      BaFileStoreExt tmpext = (BaFileStoreExt)(BaFileStoreExt)(((headers[i].getExtVos() == null) || (headers[i].getExtVos().length < 1)) ? null : headers[i].getExtVos()[0]);

      vos[i] = convertToLfwFileVo(header, tmpext);
    }
    LfwFileVO[] svos = super.getAttachFileByItemID(itempk, billtype);
    if (svos != null) {
      List list = new ArrayList();
      Collections.addAll(list, svos);
      if (vos != null)
        Collections.addAll(list, vos);
      return ((LfwFileVO[])list.toArray(new LfwFileVO[0]));
    }

    return vos;
  }

  public LfwFileVO[] getFileByItemID(String itempk)
    throws LfwBusinessException
  {
    return getAttachFileByItemID(itempk, null);
  }

  public String buildDownUrl(LfwFileVO vo)
  {
    try {
      if (vo != null) {
        if (FileStorageClient.getInstance().getFileTransfer().exist(vo.getPk_lfwfile())) {
          FileStorageClient client = FileStorageClient.getInstance();
          String clientIp = LfwRuntimeEnvironment.getClientIP();
          return client.getDownloadURL(getTrueBaModule(vo.getPk_lfwfile()), vo.getPk_lfwfile(), clientIp);
        }
        return super.buildDownUrl(vo);
      }
    } catch (FileStorageException e) {
      LfwLogger.error(e);
    }
    return "";
  }

  public String buildViewUrl(LfwFileVO vo)
  {
    try {
      if (FileStorageClient.getInstance().getFileTransfer().exist(vo.getPk_lfwfile())) {
        FileStorageClient client = FileStorageClient.getInstance();
        String clientIp = LfwRuntimeEnvironment.getClientIP();
        return client.getViewURL(getTrueBaModule(vo.getPk_lfwfile()), vo.getPk_lfwfile(), clientIp);
      }
      return super.buildViewUrl(vo);
    } catch (FileStorageException e) {
      LfwLogger.error(e);
    }
    return "";
  }

  public String buildUploadUrl()
  {
    FileStorageClient client = FileStorageClient.getInstance();
    String clientIp = LfwRuntimeEnvironment.getClientIP();
    return client.getUploadURL(getBamodule(), clientIp);
  }

  public String buildSwfPath()
  {
    String clientIp = LfwRuntimeEnvironment.getClientIP();
    return FileStorageClient.getInstance().getUploadSwfURL(clientIp);
  }

  public boolean withSession()
  {
    return false;
  }

  private String getTrueBaModule(String filePK)
  {
    String bamodule = null;
    FileHeaderCombineModel[] filevos = FileStorageClient.getInstance().getFileTransfer().queryHeaderCombModel(new String[] { filePK });
    if (!(ArrayUtils.isEmpty(filevos))) {
      bamodule = filevos[0].getFileHeader().getModule();
    }
    return bamodule;
  }
}