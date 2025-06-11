package ru.application.speechhint.ui.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

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
import ru.application.speechhint.viewmodel.ServerViewModel;
import ru.application.speechhint.viewmodel.SettingsViewModel;
import ru.application.speechhint.viewmodel.TeleprompterViewModel;

@AndroidEntryPoint
public class ServerFragment extends Fragment {

    private ServerViewModel serverViewModel;
    private TeleprompterViewModel teleprompterViewModel;
    private SettingsViewModel settingsViewModel;
    private Switch switchServer;
    private ImageView imageQr;
    private TextView textLink;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_server, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchServer = view.findViewById(R.id.switch_server);
        imageQr = view.findViewById(R.id.image_qr);
        textLink = view.findViewById(R.id.text_link);

        serverViewModel = new ViewModelProvider(requireActivity()).get(ServerViewModel.class);
        settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
        teleprompterViewModel = new ViewModelProvider(requireActivity()).get(TeleprompterViewModel.class);

        if (serverViewModel.getServerConnectionInfo() != null){
            switchServer.setChecked(true);
            showQrAndLink();
        }

        switchServer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                serverViewModel.startServer();
                serverViewModel.setServerCurrentSettings(settingsViewModel.getSettingsLiveData().getValue());
                serverViewModel.setServerCurrentDocument(teleprompterViewModel.getDocumentLiveData().getValue());
                serverViewModel.setServerCurrentPosition(teleprompterViewModel.getCurrentPositionLiveData().getValue());
                Log.i("SERVER", serverViewModel.getServerConnectionInfo().getIp() + ":" + serverViewModel.getServerConnectionInfo().getPort());
                showQrAndLink();
            } else {
                serverViewModel.stopServer();
                imageQr.setVisibility(View.GONE);
                textLink.setVisibility(View.GONE);
            }
        });
    }

    private void showQrAndLink() {
        ServerConnectionInfo info = serverViewModel.getServerConnectionInfo();
        if (info != null) {
            String url = "http://" + info.getIp() + ":" + info.getPort();
            try {
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.encodeBitmap(url, BarcodeFormat.QR_CODE, 400, 400);
                imageQr.setImageBitmap(bitmap);
                imageQr.setVisibility(View.VISIBLE);
            } catch (WriterException e) {
                imageQr.setVisibility(View.GONE);
            }
            textLink.setText(url);
            textLink.setVisibility(View.VISIBLE);
        } else {
            imageQr.setVisibility(View.GONE);
            textLink.setVisibility(View.GONE);
        }
    }
}