package com.isgm.camreport.network;

public class ResponseSuccessEvent {

    public static class ImageUploadedSuccessEvent {
        private String status;
        private String error;
        private boolean b;
        private int id;

        public ImageUploadedSuccessEvent(String status, String error, boolean b, int id) {
            this.status = status;
            this.error = error;
            this.b = b;
            this.id = id;
        }

        public String getStatus() {
            return status;
        }

        public String getError() {
            return error;
        }

        public boolean isB() {
            return b;
        }

        public int getId() {
            return id;
        }
    }
}
