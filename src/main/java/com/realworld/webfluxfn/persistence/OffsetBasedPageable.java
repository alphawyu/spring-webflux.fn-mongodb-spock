package com.realworld.webfluxfn.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class OffsetBasedPageable implements Pageable {
    private final transient int limit;
    private final transient int offset;
    private final transient Sort sort;

    private static final int MIN_LIMIT = 1;
    private static final int MIN_OFFSET = 0;

    private OffsetBasedPageable(final int limit, final int offset, final Sort sort) {
        if (limit < MIN_LIMIT) {
            throw new IllegalArgumentException("Limit must not be less than one");
        }
        if (offset < MIN_OFFSET) {
            throw new IllegalArgumentException("Offset index must not be less than zero");
        }
        this.limit = limit;
        this.offset = offset;
        this.sort = sort;
    }

//    public static Pageable of(final int limit, final int offset) {
//        return new OffsetBasedPageable(limit, offset, Sort.unsorted());
//    }

    public static Pageable makeInstance(final int limit, final int offset, final Sort sort) {
        return new OffsetBasedPageable(limit, offset, sort);
    }

    @Override
    public int getPageNumber() {
        throw unsupportedOperation();
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        throw unsupportedOperation();
    }

    @Override
    public Pageable previousOrFirst() {
        throw unsupportedOperation();
    }

    @Override
    public Pageable first() {
        throw unsupportedOperation();
    }

    @Override
    public Pageable withPage(final int pageNumber) {
        throw unsupportedOperation();
    }

    @Override
    public boolean hasPrevious() {
        throw unsupportedOperation();
    }

    private UnsupportedOperationException unsupportedOperation() {
        return new UnsupportedOperationException("OffsetBasedPageable has no pages. Contains only offset and page size");
    }
}
