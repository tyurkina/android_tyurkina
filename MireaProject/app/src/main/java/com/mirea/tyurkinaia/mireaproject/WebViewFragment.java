package com.mirea.tyurkinaia.mireaproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.mirea.tyurkinaia.mireaproject.databinding.FragmentWebviewBinding;

public class WebViewFragment extends Fragment {

    private FragmentWebviewBinding binding;
    private WebView webView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWebviewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        webView = binding.webView;

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}