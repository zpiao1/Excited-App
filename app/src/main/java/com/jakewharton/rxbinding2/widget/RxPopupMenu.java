package com.jakewharton.rxbinding2.widget;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.widget.PopupMenu;

import io.reactivex.Observable;

import static com.jakewharton.rxbinding2.internal.Preconditions.checkNotNull;

/**
 * Static factory methods for creating {@linkplain Observable observables} for {@link PopupMenu}.
 */
public final class RxPopupMenu {
    private RxPopupMenu() {
        throw new AssertionError("No instances.");
    }

    /**
     * Create an observable which emits the clicked item in {@code view}'s menu.
     * <p>
     * <em>Warning:</em> The created observable keeps a strong reference to {@code view}.
     * Unsubscribe to free this reference.
     * <p>
     * <em>Warning:</em> The created observable uses {@link PopupMenu#setOnMenuItemClickListener}
     * to observe dismiss change. Only one observable can be used for a view at a time.
     */
    @CheckResult
    @NonNull
    public static Observable<MenuItem> itemClicks(@NonNull PopupMenu view) {
        checkNotNull(view, "view == null");
        return new PopupMenuItemClickObservable(view);
    }

    /**
     * Create an observable which emits on {@code view} dismiss events. The emitted value is
     * unspecified and should only be used as notification.
     * <p>
     * <em>Warning:</em> The created observable keeps a strong reference to {@code view}.
     * Unsubscribe to free this reference.
     * <p>
     * <em>Warning:</em> The created observable uses {@link PopupMenu#setOnDismissListener} to
     * observe dismiss change. Only one observable can be used for a view at a time.
     */
    @CheckResult
    @NonNull
    public static Observable<Object> dismisses(@NonNull PopupMenu view) {
        checkNotNull(view, "view == null");
        return new PopupMenuDismissObservable(view);
    }
}
