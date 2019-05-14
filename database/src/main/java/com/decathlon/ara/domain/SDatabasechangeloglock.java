package com.decathlon.ara.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

public class SDatabasechangeloglock extends com.querydsl.sql.RelationalPathBase<SDatabasechangeloglock> {

    private static final long serialVersionUID = -1637887078;

    public static final SDatabasechangeloglock databasechangeloglock = new SDatabasechangeloglock("DATABASECHANGELOGLOCK");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath locked = createBoolean("locked");

    public final StringPath lockedby = createString("lockedby");

    public final DateTimePath<java.sql.Timestamp> lockgranted = createDateTime("lockgranted", java.sql.Timestamp.class);

    public final com.querydsl.sql.PrimaryKey<SDatabasechangeloglock> primary = createPrimaryKey(id);

    public SDatabasechangeloglock(String variable) {
        super(SDatabasechangeloglock.class, forVariable(variable), "null", "DATABASECHANGELOGLOCK");
        addMetadata();
    }

    public SDatabasechangeloglock(String variable, String schema, String table) {
        super(SDatabasechangeloglock.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public SDatabasechangeloglock(String variable, String schema) {
        super(SDatabasechangeloglock.class, forVariable(variable), schema, "DATABASECHANGELOGLOCK");
        addMetadata();
    }

    public SDatabasechangeloglock(Path<? extends SDatabasechangeloglock> path) {
        super(path.getType(), path.getMetadata(), "null", "DATABASECHANGELOGLOCK");
        addMetadata();
    }

    public SDatabasechangeloglock(PathMetadata metadata) {
        super(SDatabasechangeloglock.class, metadata, "null", "DATABASECHANGELOGLOCK");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(locked, ColumnMetadata.named("LOCKED").withIndex(2).ofType(Types.BIT).withSize(1).notNull());
        addMetadata(lockedby, ColumnMetadata.named("LOCKEDBY").withIndex(4).ofType(Types.VARCHAR).withSize(255));
        addMetadata(lockgranted, ColumnMetadata.named("LOCKGRANTED").withIndex(3).ofType(Types.TIMESTAMP).withSize(19));
    }

}

