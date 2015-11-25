//package com.yy.misaka.demo.appmodel.callback;
//
//import com.yy.misaka.demo.User;
//
///**
// * @author yangtian
// *         个人信息请求回调
// *         Date: 15/3/5
// */
//public interface ProfileCallback {
//
//    interface Info {
//        void onUser(User user);
//    }
//
//    /**
//     * 昵称请求回调
//     */
//    public interface Modify {
//        /**
//         * 修改昵称成功
//         *
//         * @param user 返回昵称修改后的user
//         */
//        void onNameModifySuccess(User user);
//
//        /**
//         * 修改昵称失败
//         *
//         * @param code         错误码
//         * @param errorMessage 错误信息
//         */
//        void onNameModifyFailure(int code, String errorMessage);
//    }
//
//    /**
//     * 头像请求回调
//     */
//    public interface Portrait {
//        /**
//         * 修改头像成功
//         *
//         * @param url 返回上传至服务器的头像文件url
//         */
//        void onPortraitUploadSuccess(String url);
//
//        /**
//         * 修改头像失败
//         *
//         * @param code         错误码
//         * @param errorMessage 错误信息
//         */
//        void onPortraitUploadFailure(int code, String errorMessage);
//
//    }
//
//}
