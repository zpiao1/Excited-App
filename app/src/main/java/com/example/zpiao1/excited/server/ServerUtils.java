package com.example.zpiao1.excited.server;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerUtils {

  public static final String BASE_URL = "http://54.169.173.170:3000/api/";
  private static Retrofit RETROFIT = null;

  public static Retrofit getRetrofit() {  // Singleton
    if (RETROFIT == null) {
      RETROFIT = new Retrofit.Builder()
          .baseUrl(BASE_URL)
          .addConverterFactory(GsonConverterFactory.create())
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .build();
    }
    return RETROFIT;
  }

  public static <T> void addToDisposable(CompositeDisposable disposable,
      Observable<T> observable,
      Consumer<T> responseConsumer,
      Consumer<Throwable> errorConsumer) {
    disposable.add(wrapObservable(observable, responseConsumer, errorConsumer));
  }

  public static <T> Disposable wrapObservable(Observable<T> observable,
      Consumer<T> responseConsumer,
      Consumer<Throwable> errorConsumer) {
    return observable.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(responseConsumer, errorConsumer);
  }
}
