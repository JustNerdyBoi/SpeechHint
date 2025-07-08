package ru.application.speechhint.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;

import ru.application.speechhint.R;

public class SubscriptionDialog {
    public static void show(Context context, Runnable onPurchaseConfirmed) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.paid_feature_header)
                .setMessage(R.string.paid_feature_body)
                .setPositiveButton(R.string.paid_feature_accept, (dialog, which) -> {
                    if (onPurchaseConfirmed != null) {
                        onPurchaseConfirmed.run();
                    }
                })
                .setNegativeButton(R.string.paid_feature_deny, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
}
