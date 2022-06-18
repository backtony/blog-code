package com.querydsl.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QZone is a Querydsl query type for Zone
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QZone extends EntityPathBase<Zone> {

    private static final long serialVersionUID = 1532290169L;

    public static final QZone zone = new QZone("zone");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath mainZone = createString("mainZone");

    public QZone(String variable) {
        super(Zone.class, forVariable(variable));
    }

    public QZone(Path<? extends Zone> path) {
        super(path.getType(), path.getMetadata());
    }

    public QZone(PathMetadata metadata) {
        super(Zone.class, metadata);
    }

}

