package com.hola.weather.utils;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import java.lang.reflect.Field;

/**
 * Created by wangxin on 17-8-28.
 */

public class FetchViewIdManager {

    private static class Holder {
        private final static FetchViewIdManager INSTANCE = new FetchViewIdManager();
    }

    private FetchViewIdManager() {

    }

    private class OnClickListenerProxy implements View.OnClickListener {

        private View.OnClickListener listener;

        public OnClickListenerProxy(View.OnClickListener listener) {
            this.listener = listener;
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                hook(v);
                listener.onClick(v);
            }
        }

        private void hook(View view) {
            String IdAsString = view.getResources().getResourceName(view.getId());
            Log.e("FetchViewIdManager", "hook view id: " + IdAsString);
        }
    }

    public static FetchViewIdManager getInstance() {
        return Holder.INSTANCE;
    }

    public void hookView(View view) {
        View.OnClickListener oldOnClickListener = getClickListener(view);
        if (oldOnClickListener == null) {
            return;
        }
        OnClickListenerProxy newOnClickListener = new OnClickListenerProxy(oldOnClickListener);
        view.setOnClickListener(newOnClickListener);
    }

    public void hookAllViews(Activity activity) {
        show_children(activity.getWindow().getDecorView());
    }

    private void show_children(View v) {
        ViewGroup viewgroup=(ViewGroup)v;
        for (int i=0;i<viewgroup.getChildCount();i++) {
            final View v1=viewgroup.getChildAt(i);
            if (v1 instanceof ViewGroup) show_children(v1);
            hookView(v1);
        }
    }

    private View.OnClickListener getClickListener(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return getClickListenerV14(view);
        } else {
            return getClickListenerV(view);
        }
    }

    /**
     * API 14 以下
     */
    private View.OnClickListener getClickListenerV(View view) {
        View.OnClickListener listener = null;
        try {
            Class<?> clazz = Class.forName("android.view.View");
            Field field = clazz.getDeclaredField("mOnClickListener");
            listener = (View.OnClickListener) field.get(view);
        } catch (ClassNotFoundException e) {
            Log.e("FetchViewIdManager", "ClassNotFoundException: " + e.getMessage());
        } catch (NoSuchFieldException e) {
            Log.e("FetchViewIdManager", "NoSuchFieldException: " + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.e("FetchViewIdManager", "IllegalAccessException: " + e.getMessage());
        }
        return listener;
    }

    /**
     * API 14 以上
     */
    private View.OnClickListener getClickListenerV14(View view) {
        View.OnClickListener listener = null;
        try {
            Class<?> clazz = Class.forName("android.view.View");
            Field field = clazz.getDeclaredField("mListenerInfo");
            Object listenerInfo = null;
            if (field != null) {
                field.setAccessible(true);
                listenerInfo = field.get(view);
            }
            Class<?> cls = Class.forName("android.view.View$ListenerInfo");
            Field declaredField = cls.getDeclaredField("mOnClickListener");
            if (declaredField != null && listenerInfo != null) {
                listener = (View.OnClickListener) declaredField.get(listenerInfo);
            }
        } catch (ClassNotFoundException e) {
            Log.e("FetchViewIdManager", "ClassNotFoundException: " + e.getMessage());
        } catch (NoSuchFieldException e) {
            Log.e("FetchViewIdManager", "NoSuchFieldException: " + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.e("FetchViewIdManager", "IllegalAccessException: " + e.getMessage());
        }
        return listener;
    }
}