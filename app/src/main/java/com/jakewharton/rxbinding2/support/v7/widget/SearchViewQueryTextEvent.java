package com.jakewharton.rxbinding2.support.v7.widget;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v7.widget.SearchView;

import com.jakewharton.rxbinding2.view.ViewEvent;

public final class SearchViewQueryTextEvent extends ViewEvent<SearchView> {
    private final CharSequence queryText;
    private final boolean submitted;

    private SearchViewQueryTextEvent(@NonNull SearchView view, @NonNull CharSequence queryText,
                                     boolean submitted) {
        super(view);
        this.queryText = queryText;
        this.submitted = submitted;
    }

    @CheckResult
    @NonNull
    public static SearchViewQueryTextEvent create(@NonNull SearchView view,
                                                  @NonNull CharSequence queryText, boolean submitted) {
        return new SearchViewQueryTextEvent(view, queryText, submitted);
    }

    @NonNull
    public CharSequence queryText() {
        return queryText;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof SearchViewQueryTextEvent)) return false;
        SearchViewQueryTextEvent other = (SearchViewQueryTextEvent) o;
        return other.view() == view()
                && other.queryText.equals(queryText)
                && other.submitted == submitted;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 37 + view().hashCode();
        result = result * 37 + queryText.hashCode();
        result = result * 37 + (submitted ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SearchViewQueryTextEvent{view="
                + view()
                + ", queryText="
                + queryText
                + ", submitted="
                + submitted
                + '}';
    }
}
