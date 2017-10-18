package com.focus.rxjavademo.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.focus.rxjavademo.R;
import com.focus.rxjavademo.service.MyService;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "my_log";
    private Subscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_request).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSubscription.request(3);
            }
        });
        findViewById(R.id.btn_start_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMyService();
            }
        });
        findViewById(R.id.btn_go_second).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goSecondActivity();
            }
        });
        findViewById(R.id.btn_bind_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bindService(new Intent(MainActivity.this, MyService.class), connection, Context.BIND_AUTO_CREATE);
            }
        });

        findViewById(R.id.btn_unbind_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unbindService(connection);
            }
        });
        findViewById(R.id.btn_stop_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(MainActivity.this, MyService.class));
            }
        });

//        testMap();
//        testFlatMap();
//        testJust();
//        testZip();
//        testBackpresure();
//        testFlowable();
//        testInterval();
        Log.d(TAG, "MainActivity onCreate thread id is : " + Thread.currentThread().getId());
    }

    private void goSecondActivity() {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }

    private void startMyService() {
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
    }

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "MainActivity onServiceConnected");

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "MainActivity onServiceDisconnected ");
        }
    };

    private void testInterval() {
        Flowable.interval(1, TimeUnit.SECONDS)
                .onBackpressureDrop()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.d(TAG, "onSubscribe");
                        mSubscription = s;
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Long aLong) {
                        Log.d(TAG, "onNext: " + aLong);
                        try {
                            Thread.sleep(1000);  //延时1秒
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.w(TAG, "onError: ", t);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete");
                    }
                });
    }

    private void testFlowable() {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                Log.d(TAG, "emit 1");
                emitter.onNext(1);
                Log.d(TAG, "emit 2");
                emitter.onNext(2);
                Log.d(TAG, "emit 3");
                emitter.onNext(3);
                Log.d(TAG, "emit complete");
                emitter.onComplete();

            }
        },
//                BackpressureStrategy.BUFFER)//无限大水缸
//                BackpressureStrategy.DROP)//丢弃部分事件
//                BackpressureStrategy.LATEST)//获取最新事件
                BackpressureStrategy.ERROR)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                Log.d(TAG, "onSubscribe");
//                s.request(Long.MAX_VALUE);  //注意这句代码
                mSubscription = s;
            }

            @Override
            public void onNext(Integer integer) {
                Log.d(TAG, "onNext: " + integer);
            }

            @Override
            public void onError(Throwable t) {
                Log.w(TAG, "onError: ", t);
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete");
            }
        });

    }

    private void testBackpresure() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                for (int i = 0; ; i++) {  //无限循环发送事件
                    emitter.onNext(i);
                    Thread.sleep(2000);
                }
            }
        })
                .subscribeOn(Schedulers.io())
//                .sample(2, TimeUnit.SECONDS) //定期发射最新的数据
//                .filter(new Predicate<Integer>() {
//                    @Override
//                    public boolean test(Integer integer) throws Exception {
//                        return integer % 10 == 0;
//                    }
//                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Thread.sleep(2*1000);
                        Log.d(TAG, "" + integer);
                    }
                });
    }

    private void testZip() {
        Observable<Integer> observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.d(TAG, "emit 1");
                emitter.onNext(1);
                Thread.sleep(1000);
                Log.d(TAG, "emit 2");
                emitter.onNext(2);
                Thread.sleep(1000);
                Log.d(TAG, "emit 3");
                emitter.onNext(3);
                Thread.sleep(1000);
                Log.d(TAG, "emit 4");
                emitter.onNext(4);
                Thread.sleep(1000);
                Log.d(TAG, "emit complete1");
                emitter.onComplete();
            }
        }).observeOn(Schedulers.io());

        Observable<String> observable2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                Log.d(TAG, "emit A");
                emitter.onNext("A");
                Thread.sleep(1000);
                Log.d(TAG, "emit B");
                emitter.onNext("B");
                Thread.sleep(1000);
                Log.d(TAG, "emit C");
                emitter.onNext("C");
                Thread.sleep(1000);
                Log.d(TAG, "emit complete2");
                emitter.onComplete();
            }
        }).observeOn(Schedulers.io());

        Observable.zip(observable1, observable2, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(Integer integer, String s) throws Exception {
                return integer + s;
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe");
            }

            @Override
            public void onNext(String value) {
                Log.d(TAG, "onNext: " + value);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete");
            }
        });

    }

    public void testJust() {
        Observable.just("hello world!")
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .map(new Function<String, String>() {
                    @Override
                    public String apply(String s) throws Exception {
                        Log.d(TAG,  "map thread is : " + Thread.currentThread().getName());
                        return s + "handled !";
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.d(TAG,  "subscribe thread is : " + Thread.currentThread().getName());
                        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void testFlatMap() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
//                Log.d(TAG,  "Observable thread is : " + Thread.currentThread().getName());
//                Log.d(TAG, "emit 1");
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
            }
        }).flatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                final List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("I am value " + integer);
                }
                return Observable.fromIterable(list).delay(10, TimeUnit.MILLISECONDS);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.d(TAG, s);
            }
        });

    }

    public static void testMap() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
//                Log.d(TAG,  "Observable thread is : " + Thread.currentThread().getName());
//                Log.d(TAG, "emit 1");
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
            }
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) throws Exception {
                return "this is result " + integer;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String string) throws Exception {
//                Thread.sleep(1000);
                Log.d(TAG,  string);
            }
        });

    }
}
