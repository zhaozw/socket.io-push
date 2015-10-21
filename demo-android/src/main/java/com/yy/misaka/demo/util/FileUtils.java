//package com.yy.misaka.demo.util;
//
//import android.graphics.Bitmap;
//import android.os.Environment;
//import android.text.TextUtils;
//import com.yy.androidlib.util.http.BasicFileUtils;
//
//import java.io.*;
//
//public class FileUtils {
//
//    private FileOutputStream mFileOutputStream = null;
//    private BufferedOutputStream mBufferedOutputStream = null;
//    private File mFile;
//    public static final String IMAGE_DIR = android.os.Environment.getExternalStorageDirectory()+"/Sleep/";
//    public static final String IMAGE_NAME = "share.png";
//    public class FileUtilsException extends Exception {
//        public FileUtilsException(String description) {
//            super(description);
//        }
//    }
//
//    private FileUtils(File file, FileOutputStream fileOutputStream) throws FileNotFoundException, FileUtilsException {
//        mFile = file;
//        mFileOutputStream = fileOutputStream;
//        if (mFile != null) {
//            if (mFileOutputStream == null) {
//                mFileOutputStream = new FileOutputStream(mFile);
//            }
//            mBufferedOutputStream = new BufferedOutputStream(mFileOutputStream);
//        } else {
//            throw new FileUtilsException(
//                    "YYFileOutput, can not create file output stream");
//        }
//    }
//
//    public void write(Bitmap bmp, int compressRate) {
//        bmp.compress(Bitmap.CompressFormat.JPEG, compressRate, mBufferedOutputStream);
//    }
//
//    public void close() {
//        try {
//            if (mBufferedOutputStream != null) {
//                mBufferedOutputStream.flush();
//                mBufferedOutputStream.close();
//            }
//            if (mFileOutputStream != null) {
//                mFileOutputStream.close();
//            }
//        } catch (IOException e) {
//        }
//    }
//
//    public static boolean isSDCardExit() {
//        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
//    }
//
//    public static FileUtils openFile(String filePath) throws IOException, FileUtilsException {
//        String dirPath = filePath.substring(0, filePath.lastIndexOf(File.separator));
//        BasicFileUtils.createDir(dirPath, true);
//
//        File file = new File(filePath);
//        if (!file.exists() && !file.createNewFile()) {
//            file = null;
//        }
//        return new FileUtils(file, null);
//    }
//
//    public static String getDirOfFilePath(String filePath) {
//        if (TextUtils.isEmpty(filePath)) {
//            return null;
//        }
//        int sepPos = filePath.lastIndexOf(File.separatorChar);
//        if (sepPos == -1) {
//            return null;
//        }
//        return filePath.substring(0, sepPos);
//    }
//
//    public static String getFileName(String filePath) {
//        if (TextUtils.isEmpty(filePath)) {
//            return null;
//        }
//        int sepPos = filePath.lastIndexOf(File.separator) + 1;
//        return filePath.substring(sepPos);
//    }
//
//    public static void WriteSharePiceure(Bitmap bitmap){
//        File destDir = new File(IMAGE_DIR);
//        if (!destDir.exists()) {
//            destDir.mkdirs();
//        }
//        File newFile = new File(IMAGE_DIR + IMAGE_NAME);
//        FileOutputStream fos=null;
//        try {
//            if (!newFile.exists()){
//                newFile.createNewFile();
//                fos = new FileOutputStream(newFile);
//                bitmap.compress(Bitmap.CompressFormat.PNG,50,fos);
//                fos.flush();
//            }
//            else return;
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }finally{
//            try {
//                if(fos!=null)fos.close();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//
//    }
//}
