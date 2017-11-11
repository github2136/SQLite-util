package com.github2136.sqlite;

import com.github2136.sqliteutil.Column;
import com.github2136.sqliteutil.Table;

/**
 * Created by yubin on 2017/8/22.
 */
@Table
public class Entity {
    @Column()
    private String id;
    @Column()
    private String name;
    @Column(version = 2)
    private String name2;
    @Column(version = 3, defaultVal = "abc", notNull = true, primaryKey = true)
    private String name3;
}
