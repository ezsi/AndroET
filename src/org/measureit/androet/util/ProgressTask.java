/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.measureit.androet.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public abstract class ProgressTask extends AsyncTask<String, Void, Boolean> {
        
        private Context context = null;
        private ProgressDialog progressDialog = null;
        private String message;
        
        public ProgressTask(Context context, String message) {
                this.context = context;
                this.message = message;
        }

        @Override
        protected void onPostExecute(Boolean result) {
                this.progressDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
                this.progressDialog = ProgressDialog.show(this.context, "Please wait ...", message, true);
        }

//        @Override
//        protected Boolean doInBackground(String... params) {
//                if (params.length > 1) {
//                        String loginName = params[0];
//                        String loginPass = params[1];
//                        try {
//                                // Belepteto logika
//                                Thread.sleep(2000);
//                        } catch (Exception exception) {
//                                this.error = exception;
//                                return false;
//                        }
//                        return true;
//                } else {
//                        return false;
//                }
//        }
        
}