//package com.yy.misaka.demo.util;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.BitmapFactory.Options;
//import android.graphics.Matrix;
//import android.graphics.Rect;
//import android.text.TextUtils;
//import android.util.Log;
//
//import java.io.BufferedInputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//
//public class YYImageUtils {
//
//    public static final int IMAGE_COMPRESS_RATE = 25;
//    // resize and rotate image, if matrix is null, then no rotate will be done
//    public static boolean resizeAndRotateImage(String imageFile, String newFileName, int maxWidth, int maxHeight, Matrix matrix) {
//        Options options = new Options();
//        options.outHeight = 0;
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(imageFile, options);
//        if (options.outWidth <= 0 || options.outHeight <= 0) {
//            return false;
//        }
//        options.inJustDecodeBounds = false;
//
//        int max = options.outWidth;
//        int min = options.outHeight;
//        if (options.outWidth < options.outHeight) {
//            max = options.outHeight;
//            min = options.outWidth;
//        }
//        int sampleSize = 1;
//        int nextMax = max >> 1;
//        int nextMin = min >> 1;
//
//        // width is supposed to be bigger in general, but if it is not, just reverse them
//        if (maxWidth < maxHeight) {
//            int temp = maxWidth;
//            maxWidth = maxHeight;
//            maxHeight = temp;
//        }
//        while (nextMax >= maxWidth && nextMin >= maxHeight) {
//            sampleSize <<= 1;
//            nextMax >>= 1;
//            nextMin >>= 1;
//        }
//        options.inSampleSize = sampleSize;
//
//        Bitmap bitmap = null;
//        try {
//            bitmap = BitmapFactory.decodeFile(imageFile, options);
//            if (matrix != null) {
//                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//            }
//        } catch (OutOfMemoryError e) {
//            Log.d("YYImageUtils.class", e.toString());
//        }
//        if (bitmap != null) {
//            try {
//                FileUtils out = FileUtils.openFile(newFileName);
//                out.write(bitmap, IMAGE_COMPRESS_RATE);
//                out.close();
//                return true;
//            } catch (FileUtils.FileUtilsException e) {
//                Log.d("YYImageUtils.class", e.toString());
//            } catch (IOException e) {
//                Log.d("YYImageUtils.class", e.toString());
//            }
//        }
//        return false;
//    }
//
//
//    public static Bitmap decodeBySize(String filePath, int size) {
//        Rect rect = decodeBmpSize(filePath);
//        if (rect.width() > rect.height()) {
//            return decodeByWidth(filePath, size);
//        } else {
//            return decodeByHeight(filePath, size);
//        }
//    }
//
//    public static Bitmap decodeByWidth(String filePath, int desiredWidth) {
//        try {
//            return decodeFileOrThrow(filePath, desiredWidth, 0);
//        } catch (Exception e) {
//            Log.e("decodeByWidth Exception", e.toString());
//            return null;
//        }
//    }
//
//    public static Bitmap decodeByHeight(String filePath, int desiredHeight) {
//        try {
//            return decodeFileOrThrow(filePath, 0, desiredHeight);
//        } catch (Exception e) {
//            Log.e("decodeByHeight Exception", e.toString());
//            return null;
//        }
//    }
//
//    /**
//     * Decode file with given options.
//     * Will prefer use a smaller sample size to save memory,
//     * If this is not up to demand, use the one with more parameter:
//     * {@link #decodeFileOrThrow(String, int, int, boolean)}.
//     * NOTE OutOfMemoryError can be throw here.
//     *
//     * @param filePath      File path.
//     * @param desiredWidth  Desired width, can be 0.
//     *                      If set to 0, desiredHeight will be honored.
//     *                      If both desiredWidth and desiredHeight are 0,
//     *                      the original bitmap will be decoded.
//     * @param desiredHeight Desired height, can be 0.
//     *                      If set to 0, desiredWidth will be honored.
//     *                      If both desiredWidth and desiredHeight are 0,
//     *                      the original bitmap will be decoded.
//     * @return Bitmap decoded, or null if failed.
//     */
//    public static Bitmap decodeFileOrThrow(String filePath, int desiredWidth, int desiredHeight) {
//        return decodeFileOrThrow(filePath, desiredWidth, desiredHeight, true);
//    }
//
//    /**
//     * Decode file with given options.
//     * NOTE OutOfMemoryError can be throw here.
//     *
//     * @param filePath      File path.
//     * @param desiredWidth  Desired width, can be 0.
//     *                      If set to 0, maximum width will be used,
//     *                      i.e. : desiredHeight will take effect.
//     *                      If both desiredWidth and desiredHeight are 0,
//     *                      the original bitmap will be decoded.
//     * @param desiredHeight Desired height, can be 0.
//     *                      If set to 0, maximum height will be used.
//     *                      i.e. : desiredWidth will take effect.
//     *                      If both desiredWidth and desiredHeight are 0,
//     *                      the original bitmap will be decoded.
//     * @param isMemoryPrior If true, will prefer to use a bigger sample size
//     *                      to use less memory, otherwise prefer to use a smaller
//     *                      sample size, the the returned bitmap can be with bigger size,
//     *                      and can be probably more vivid.
//     * @return Bitmap decoded, or null if failed.
//     */
//    public static Bitmap decodeFileOrThrow(String filePath,
//                                           int desiredWidth, int desiredHeight, boolean isMemoryPrior) {
//        Options opts = getProperOptions(filePath, desiredWidth, desiredHeight, isMemoryPrior);
//        if (opts == null) {
//            return null;
//        }
//        opts.inJustDecodeBounds = false;
//        return BitmapFactory.decodeFile(filePath, opts);
//    }
//
//    private static Options getProperOptions(String filePath, int desiredWidth, int desiredHeight,
//                                            boolean isMemoryPrior) {
//        Options opts = new Options();
//        opts.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(filePath, opts);
//        if (opts.outWidth <= 0 || opts.outHeight <= 0) {
//            return null;
//        }
//
//        int sampleSize = calSampleSize(desiredWidth, desiredHeight, isMemoryPrior, opts);
//
//        if (desiredHeight > 0 || desiredWidth > 0) {
//            do {
//                opts.inSampleSize = sampleSize;
//                BitmapFactory.decodeFile(filePath, opts);
//                sampleSize++;
//            }
//            while ((desiredWidth > 0 && opts.outWidth > desiredWidth)
//                    || (desiredHeight > 0 && opts.outHeight > desiredHeight));
//        }
//        return opts;
//    }
//
//    private static int calSampleSize(int desiredWidth, int desiredHeight, boolean isMemoryPrior, Options opts) {
//        int sampleSize;
//        if (desiredWidth == 0 && desiredHeight == 0) {
//            sampleSize = 1;
//        } else if (desiredHeight == 0) {
//            sampleSize = (opts.outWidth + desiredWidth - 1) / desiredWidth;
//        } else if (desiredWidth == 0) {
//            sampleSize = (opts.outHeight + desiredHeight - 1) / desiredHeight;
//        } else {
//            final int horRatio = (opts.outWidth + desiredWidth - 1) / desiredWidth;
//            final int verRatio = (opts.outHeight + desiredHeight - 1) / desiredHeight;
//            sampleSize = isMemoryPrior ? Math.max(horRatio, verRatio) : Math.min(horRatio, verRatio);
//        }
//        return sampleSize;
//    }
//
//    public static Rect decodeBmpSize(String filePath) {
//        Options opts = new Options();
//        opts.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(filePath, opts);
//        return new Rect(0, 0, opts.outWidth, opts.outHeight);
//    }
//
//    public static boolean isImage(File file) {
//        return file != null && isImage(file.getPath());
//    }
//
//    public static boolean isImage(String imageFile) {
//        if (TextUtils.isEmpty(imageFile)) {
//            return false;
//        }
//        Options options = new Options();
//        options.outHeight = 0;
//        options.inJustDecodeBounds = true;
//        try {
//            BitmapFactory.decodeFile(imageFile, options);
//            return options.outWidth > 0 && options.outHeight > 0;
//        } catch (Exception e) {
//            Log.v("YYImageUtils", "%s isn't image file " + imageFile.toString());
//            return false;
//        }
//    }
//
//    public static Bitmap resize(Bitmap oriBitmap, int targetWidth, int targetHeight) {
//        if (oriBitmap == null) {
//            return null;
//        }
//        int width = oriBitmap.getWidth();
//        int height = oriBitmap.getHeight();
//        float scaleWidth = ((float) targetWidth) / width;
//        float scaleHeight = ((float) targetHeight) / height;
//        float scale = scaleWidth > scaleHeight ? scaleHeight : scaleWidth;
//        Matrix matrix = new Matrix();
//        matrix.postScale(scale, scale);
//        try {
//            return Bitmap.createBitmap(oriBitmap, 0, 0, width, height, matrix, true);
//        } catch (OutOfMemoryError e) {
//            Log.e("YYImageUtils.class", "resizeBitmap OOM %s", e);
//        }
//        return null;
//    }
//
//    public static Bitmap resizeBitmap(Bitmap bitmap, int maxBorderLength, boolean recycle) {
//        if (bitmap == null) {
//            return null;
//        }
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        int newHeight = 0;
//        int newWidth = 0;
//        if (width > height) {
//            float ratio = ((float) height) / ((float) width);
//            newWidth = maxBorderLength;
//            newHeight = (int) ((newWidth) * ratio);
//        } else if (height > width) {
//            float ratio = ((float) width) / ((float) height);
//            newHeight = maxBorderLength;
//            newWidth = (int) ((newHeight) * ratio);
//        } else {
//            newWidth = maxBorderLength;
//            newHeight = maxBorderLength;
//        }
//        float scaleWidth = ((float) newWidth) / width;
//        float scaleHeight = ((float) newHeight) / height;
//        Matrix matrix = new Matrix();
//        matrix.postScale(scaleWidth, scaleHeight);
//        try {
//            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
//                    matrix, true);
//            if (recycle && !bitmap.isRecycled() && bitmap != resizedBitmap) {
//                bitmap.recycle();
//            }
//            return resizedBitmap;
//        } catch (Exception e) {
//            Log.e("YYImageUtils.class", "lcy resizeBitmap OOM %s", e);
//        }
//        return null;
//    }
//
//    public static Bitmap revisionImageSize(String path) throws IOException {
//        BufferedInputStream in = new BufferedInputStream(new FileInputStream(
//                new File(path)));
//        Options options = new Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeStream(in, null, options);
//        in.close();
//        int i = 0;
//        Bitmap bitmap = null;
//        while (true) {
//            if ((options.outWidth >> i <= 256)
//                    && (options.outHeight >> i <= 256)) {
//                in = new BufferedInputStream(
//                        new FileInputStream(new File(path)));
//                options.inSampleSize = (int) Math.pow(2.0D, i);
//                options.inJustDecodeBounds = false;
//                bitmap = BitmapFactory.decodeStream(in, null, options);
//                break;
//            }
//            i += 1;
//        }
//        return bitmap;
//    }
//}
