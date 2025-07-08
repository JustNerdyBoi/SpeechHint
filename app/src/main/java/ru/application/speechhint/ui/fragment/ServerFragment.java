package ru.application.speechhint.ui.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import dagger.hilt.android.AndroidEntryPoint;
import ru.application.domain.entity.ServerConnectionInfo;
import ru.application.speechhint.R;
import ru.application.speechhint.databinding.FragmentServerBinding;
import ru.application.speechhint.ui.dialog.SubscriptionDialog;
import ru.application.speechhint.viewmodel.BillingViewModel;
import ru.application.speechhint.viewmodel.ServerViewModel;
import ru.application.speechhint.viewmodel.SettingsViewModel;
import ru.application.speechhint.viewmodel.TeleprompterViewModel;

@AndroidEntryPoint
public class ServerFragment extends Fragment {

    private FragmentServerBinding binding;
    private ServerViewModel serverViewModel;
    private TeleprompterViewModel teleprompterViewModel;
    private SettingsViewModel settingsViewModel;
    private BillingViewModel billingViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentServerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        serverViewModel = new ViewModelProvider(requireActivity()).get(ServerViewModel.class);
        settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
        teleprompterViewModel = new ViewModelProvider(requireActivity()).get(TeleprompterViewModel.class);
        billingViewModel = new ViewModelProvider(requireActivity()).get(BillingViewModel.class);

        if (serverViewModel.getServerConnectionInfo() != null) {
            binding.switchServer.setChecked(true);
            showQrAndLink();
        }

        binding.switchServer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                switch (billingViewModel.checkSubscription("remote_control_purchase")) {
                    case ACTIVE:
                        serverViewModel.startServer();
                        serverViewModel.setServerCurrentSettings(settingsViewModel.getSettingsLiveData().getValue());
                        serverViewModel.setServerCurrentDocument(teleprompterViewModel.getDocumentLiveData().getValue());
                        serverViewModel.setServerCurrentPosition(teleprompterViewModel.getCurrentPositionLiveData().getValue());

                        ServerConnectionInfo info = serverViewModel.getServerConnectionInfo();
                        if (info != null) {
                            Log.i("SERVER", info.getIp() + ":" + info.getPort());
                        }

                        showQrAndLink();
                        break;

                    case NOT_ACTIVE:
                        binding.switchServer.setChecked(false);
                        SubscriptionDialog.show(requireContext(), () -> billingViewModel.purchaseSubscription("remote_control_purchase"));
                        break;

                    case NO_DATA:
                        Toast.makeText(requireContext(), R.string.paid_feature_update_toast, Toast.LENGTH_SHORT).show();
                        break;
                }
            } else {
                serverViewModel.stopServer();
                binding.imageQr.setVisibility(View.GONE);
                binding.textLink.setVisibility(View.GONE);
            }
        });
    }

    private void showQrAndLink() {
        ServerConnectionInfo info = serverViewModel.getServerConnectionInfo();
        if (info != null) {
            String url = "http://" + info.getIp() + ":" + info.getPort() + "/";
            try {
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.encodeBitmap(url, BarcodeFormat.QR_CODE, 400, 400);
                binding.imageQr.setImageBitmap(bitmap);
                binding.imageQr.setVisibility(View.VISIBLE);
            } catch (WriterException e) {
                binding.imageQr.setVisibility(View.GONE);
            }
            binding.textLink.setText(url);
            binding.textLink.setVisibility(View.VISIBLE);
        } else {
            binding.imageQr.setVisibility(View.GONE);
            binding.textLink.setVisibility(View.GONE);
        }
    }
}