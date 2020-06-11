package com.isgm.camreport.utility;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
public class ShowAlerts {

    public static void showAlert(Context context, String title, String msg, int icon) {
        AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setTitle(title);
        dialog.setMessage(msg);
        dialog.setIcon(icon);
        dialog.setButton(Dialog.BUTTON_POSITIVE,"OK",(dialog1, which) -> dialog.dismiss());
        dialog.show();
    }
}
