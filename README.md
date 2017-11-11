[![](https://jitpack.io/v/github2136/SQLite-util.svg)](https://jitpack.io/#github2136/SQLite-util)
首先引用SQLite-util   
**compile 'com.github.github2136.SQLite-util:sqlite-util:5a8f7fad4b'**   
**annotationProcessor 'com.github.github2136.SQLite-util:sqlite-util-compiler:5a8f7fad4b'**   
给实体类增加注解
```
@Table
public class Entity {
    @Column()
    private String id;
    @Column()
    private String name;
    @Column(version = 2)
    private String name2;
    @Column(version = 3, defaultVal = "abc", notNull = true)
    private String name3;
}
```
表创建语句
```
TableUtil.getCreateSQL(Entity.class)
```
版本号默认为1，每次更新添加字段时指定version版本号更新的字段不可添加PRIMARY_KEY（主键）UNIQUE（唯一）约束。版本更新语句
```
TableUtil.getUpdateSQL(Entity.class, oldVersion, newVersion)
```
使用增删查改需要继承`BaseSQLData`