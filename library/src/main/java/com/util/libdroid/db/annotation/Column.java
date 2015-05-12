package com.util.libdroid.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    public enum ConflictAction {
        ROLLBACK, ABORT, FAIL, IGNORE, REPLACE
    }

    public enum ForeignKeyAction {
        SET_NULL, SET_DEFAULT, CASCADE, RESTRICT, NO_ACTION
    }

    public String name() default "";

    public int length() default -1;

    public boolean index() default false;

    public boolean indexGroups() default false;

    public boolean notNull() default false;

    public ConflictAction onNullConflict() default ConflictAction.FAIL;

    public ForeignKeyAction onDelete() default ForeignKeyAction.NO_ACTION;

    public ForeignKeyAction onUpdate() default ForeignKeyAction.NO_ACTION;

    public boolean unique() default false;

    public ConflictAction onUniqueConflict() default ConflictAction.FAIL;

}
