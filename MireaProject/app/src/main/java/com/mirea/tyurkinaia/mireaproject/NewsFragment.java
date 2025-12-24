package com.mirea.tyurkinaia.mireaproject;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mirea.tyurkinaia.mireaproject.databinding.FragmentNewsBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import java.util.concurrent.TimeUnit;
import java.util.List;

public class NewsFragment extends Fragment {

    private FragmentNewsBinding binding;
    private NewsAdapter newsAdapter;
    private static final String API_KEY = "d22aa3a262fd49b5ae959cd27880fb7a";
    private static final String BASE_URL = "https://newsapi.org/v2/";
    private static final String TAG = "NewsFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNewsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        newsAdapter = new NewsAdapter();
        recyclerView.setAdapter(newsAdapter);
        loadNews();
        binding.refreshButton.setOnClickListener(v -> loadNews());
        return root;
    }

    private void loadNews() {
        String urlString = "https://newsapi.org/v2/top-headlines?country=us&apiKey=" + API_KEY;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://newsapi.org/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NewsApiService service = retrofit.create(NewsApiService.class);
        Call<NewsResponse> call = service.getTopHeadlines("us", API_KEY);
        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsResponse> call,
                                   @NonNull Response<NewsResponse> response) {
                showProgress(false);

                Log.d(TAG, "Получен ответ. Код: " + response.code());

                if (response.isSuccessful()) {
                    NewsResponse newsResponse = response.body();
                    if (newsResponse != null) {
                        List<NewsArticle> articles = newsResponse.getArticles();
                        Log.d(TAG, "Статус: " + newsResponse.getStatus() +
                                ", Найдено новостей: " + articles.size());

                        if (!articles.isEmpty()) {
                            newsAdapter.setArticles(articles);
                            binding.emptyTextView.setVisibility(View.GONE);

                            Toast.makeText(getContext(),
                                    "Загружено " + articles.size() + " новостей",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            binding.emptyTextView.setVisibility(View.VISIBLE);
                            binding.emptyTextView.setText("Новости не найдены");
                        }
                    } else {
                        showError("Пустой ответ от сервера");
                    }
                } else {
                    String errorMsg = "Ошибка сервера: " + response.code();
                    if (response.code() == 401) {
                        errorMsg = "Неверный API ключ (401). Получите ключ на newsapi.org";
                    } else if (response.code() == 429) {
                        errorMsg = "Превышен лимит запросов (429)";
                    }
                    showError(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                showProgress(false);
                String errorMsg = "Ошибка сети: " + t.getMessage();
                showError(errorMsg);
                Log.e(TAG, "Ошибка запроса", t);
                Toast.makeText(getContext(),
                        "Проверьте подключение к интернету",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgress(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.refreshButton.setEnabled(!show);
    }

    private void showError(String error) {
        binding.errorTextView.setText(error);
        binding.errorTextView.setVisibility(View.VISIBLE);
        Log.e(TAG, error);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}