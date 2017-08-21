package com.github2136.sqliteutil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by yubin on 2017/7/24.
 */

public abstract class BaseSQLData<T> {
    protected String formatStr = "yyyy-MM-dd HH:mm:ss:SSS";
    protected SimpleDateFormat dateFormat;
    protected SQLiteOpenHelper mSQLHelper;
    private Class<T> clazzT;

    public BaseSQLData(Context context) {
        dateFormat = new SimpleDateFormat(formatStr, Locale.CHINA);
        mSQLHelper = getSQLHelper(context);
        clazzT = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public abstract SQLiteOpenHelper getSQLHelper(Context context);


    /**
     * 插入数据
     *
     * @param t
     * @return
     */
    public boolean insert(T t) {
        SQLiteDatabase dbWrite = mSQLHelper.getWritableDatabase();
        String tableName;
        Table table = null;
        if (clazzT.isAnnotationPresent(Table.class)) {
            table = clazzT.getAnnotation(Table.class);
        }
        if (table == null) {
            throw new RuntimeException("No Table annotations in class " + clazzT.getName());
        }
        if (table.tableName().equals("")) {
            tableName = clazzT.getSimpleName();
        } else {
            tableName = table.tableName();
        }
        List<Field> fields = new ArrayList<>();
        Field[] f = clazzT.getDeclaredFields();
        fields.addAll(getDataField(f));
//        Class clazz = null;
//        do {
//            if (clazz == null) {
//                clazz = t.getSuperclass();
//            } else {
//                clazz = clazz.getSuperclass();
//            }
//            f = clazz.getDeclaredFields();
//            fields.addAll(Arrays.asList(f));
//        } while (!clazz.getName().equals("java.lang.Object"));
        ContentValues cv = getContentValues(t, fields);
        long result = dbWrite.insert(tableName, null, cv);
        dbWrite.close();
        if (cv != null) {
            return result > 0;
        } else {
            return false;
        }
    }

    /**
     * 插入数据
     *
     * @param t
     * @return
     */
    public boolean insert(List<T> t) {
        SQLiteDatabase dbWrite = mSQLHelper.getWritableDatabase();
        dbWrite.beginTransaction();
        String tableName;
        Table table = null;
        if (t != null && !t.isEmpty()) {
            if (t.get(0).getClass().isAnnotationPresent(Table.class)) {
                table = t.get(0).getClass().getAnnotation(Table.class);
            }
            if (table == null) {
                throw new RuntimeException("No Table annotations in class " + t.get(0).getClass().getName());
            }
        }else{
            throw new RuntimeException("List is empty  " + t.get(0).getClass().getName());
        }
        if (table.tableName().equals("")) {
            tableName = t.getClass().getSimpleName();
        } else {
            tableName = table.tableName();
        }
        List<Field> fields = new ArrayList<>();
        Field[] f = t.getClass().getDeclaredFields();
        fields.addAll(getDataField(f));
//        Class clazz = null;
//        do {
//            if (clazz == null) {
//                clazz = t.getSuperclass();
//            } else {
//                clazz = clazz.getSuperclass();
//            }
//            f = clazz.getDeclaredFields();
//            fields.addAll(Arrays.asList(f));
//        } while (!clazz.getName().equals("java.lang.Object"));
        int result = 0;
        for (T d1 : t) {
            ContentValues cv = getContentValues(d1, fields);
            if (cv != null && dbWrite.insert(tableName, null, cv) > 0) {
                result++;
            }
        }
        if (result == t.size()) {
            dbWrite.setTransactionSuccessful();
        } else {
            result = 0;
        }
        dbWrite.endTransaction();
        dbWrite.close();
        return result > 0;
    }

    /**
     * 查询所有数据
     *
     * @return
     */
    public List<T> query(String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        SQLiteDatabase dbRead = mSQLHelper.getReadableDatabase();
        String tableName;
        Table table = null;
        if (clazzT.isAnnotationPresent(Table.class)) {
            table = clazzT.getAnnotation(Table.class);
        }
        if (table == null) {
            throw new RuntimeException("No Table annotations in class " + clazzT.getName());
        }
        if (table.tableName().equals("")) {
            tableName = clazzT.getSimpleName();
        } else {
            tableName = table.tableName();
        }
        List<Field> fields = new ArrayList<>();
        Field[] f = clazzT.getDeclaredFields();
        fields.addAll(getDataField(f));
//        Class clazz = null;
//        do {
//            if (clazz == null) {
//                clazz = t.getSuperclass();
//            } else {
//                clazz = clazz.getSuperclass();
//            }
//            f = clazz.getDeclaredFields();
//            fields.addAll(Arrays.asList(f));
//        } while (!clazz.getName().equals("java.lang.Object"));
        Cursor cursor = dbRead.query(tableName, getColumns(fields), selection, selectionArgs, groupBy, having, orderBy, limit);
        List<T> dArrayList = new ArrayList<>();
        try {
            if (cursor != null && cursor.moveToFirst()) {
                Map<String, Integer> columnIndex = getColumnIndex(fields, cursor);
                do {
                    dArrayList.add(getData(columnIndex, cursor));
                } while (cursor.moveToNext());
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
            return dArrayList;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return dArrayList;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return dArrayList;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            dbRead.close();
        }
        return dArrayList;
    }

    public List<T> query() {
        return query(null, null, null, null, null, null);
    }

    public T queryByPrimaryKey(String primaryKey) {
        List<T> dArrayList;
        List<Field> fields = new ArrayList<>();
        Field[] f = clazzT.getDeclaredFields();
        fields.addAll(getDataField(f));
        String pk = getPrimaryKeyField(fields);
        String selection = null;
        String[] selectionArgs = null;
        if (pk != null) {
            selection = pk + "=? ";
            selectionArgs = new String[]{primaryKey};
        }
        dArrayList = query(selection, selectionArgs, null, null, null, null);
        return !dArrayList.isEmpty() ? dArrayList.get(0) : null;
    }

    /**
     * 更新
     *
     * @param t
     * @return
     */
    public boolean updateByPrimaryKey(T t) {
        SQLiteDatabase dbWrite = mSQLHelper.getWritableDatabase();
        String tableName;
        Table table = null;
        if (clazzT.isAnnotationPresent(Table.class)) {
            table = clazzT.getAnnotation(Table.class);
        }
        if (table == null) {
            throw new RuntimeException("No Table annotations in class " + clazzT.getName());
        }
        if (table.tableName().equals("")) {
            tableName = clazzT.getSimpleName();
        } else {
            tableName = table.tableName();
        }
        List<Field> fields = new ArrayList<>();
        Field[] f = clazzT.getDeclaredFields();
        fields.addAll(getDataField(f));
//        Class clazz = null;
//        do {
//            if (clazz == null) {
//                clazz = t.getSuperclass();
//            } else {
//                clazz = clazz.getSuperclass();
//            }
//            f = clazz.getDeclaredFields();
//            fields.addAll(Arrays.asList(f));
//        } while (!clazz.getName().equals("java.lang.Object"));
        String pk = getPrimaryKeyField(fields);
        String selection = null;
        String[] selectionArgs = null;
        if (pk != null) {
            selection = pk + "=? ";
            selectionArgs = new String[]{getPrimaryKeyValue(t, fields)};
        }
        ContentValues cv = getContentValues(t, fields);
        int result = dbWrite.update(tableName, cv, selection, selectionArgs);
        dbWrite.close();
        return result > 0;
    }

    public boolean deleteByPrimaryKey(Object primaryKey) {
        SQLiteDatabase dbWrite = mSQLHelper.getWritableDatabase();
        String tableName;
        Table table = null;
        if (clazzT.isAnnotationPresent(Table.class)) {
            table = clazzT.getAnnotation(Table.class);
        }
        if (table == null) {
            throw new RuntimeException("No Table annotations in class " + clazzT.getName());
        }
        if (table.tableName().equals("")) {
            tableName = clazzT.getSimpleName();
        } else {
            tableName = table.tableName();
        }
        List<Field> fields = new ArrayList<>();
        Field[] f = clazzT.getDeclaredFields();
        fields.addAll(getDataField(f));
//        Class clazz = null;
//        do {
//            if (clazz == null) {
//                clazz = t.getSuperclass();
//            } else {
//                clazz = clazz.getSuperclass();
//            }
//            f = clazz.getDeclaredFields();
//            fields.addAll(Arrays.asList(f));
//        } while (!clazz.getName().equals("java.lang.Object"));
        String pk = getPrimaryKeyField(fields);
        String selection = null;
        String[] selectionArgs = null;
        if (pk != null) {
            selection = pk + "=? ";
            selectionArgs = new String[]{primaryKey.toString()};
        }
        int result = dbWrite.delete(tableName, selection, selectionArgs);
        dbWrite.close();
        return result > 0;
    }

    private ContentValues getContentValues(T t, List<Field> fields) {
        ContentValues cv = new ContentValues();
        try {
            for (Field field : fields) {
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    String columnName = column.columnName();
                    if (columnName.equals("")) {
                        columnName = field.getName();
                    }
                    Column.Type columnType = column.columnType();
                    field.setAccessible(true);
                    Object value = field.get(t);
                    if (value != null) {
                        switch (columnType) {
                            case STRING: {
                                String str = String.valueOf(value);
                                cv.put(columnName, str);
                            }
                            break;
                            case INTEGER: {
                                int intVal = Integer.parseInt(String.valueOf(value));
                                cv.put(columnName, intVal);
                            }
                            break;
                            case BOOLEAN: {
                                boolean bolVal = Boolean.parseBoolean(String.valueOf(value));
                                cv.put(columnName, bolVal);
                            }
                            break;
                            case SHORT: {
                                short shortVal = Short.parseShort(String.valueOf(value));
                                cv.put(columnName, shortVal);
                            }
                            break;
                            case LONG: {
                                long longVal = Long.parseLong(String.valueOf(value));
                                cv.put(columnName, longVal);
                            }
                            break;
                            case BYTE: {
                                byte byteVal = Byte.parseByte(String.valueOf(value));
                                cv.put(columnName, byteVal);
                            }
                            break;
                            case BYTES: {
                                byte[] bytesVal = (byte[]) value;
                                cv.put(columnName, bytesVal);
                            }
                            break;
                            case FLOAT: {
                                float floatVal = Float.parseFloat(String.valueOf(value));
                                cv.put(columnName, floatVal);
                            }
                            break;
                            case DOUBLE: {
                                double doubleVal = Double.parseDouble(String.valueOf(value));
                                cv.put(columnName, doubleVal);
                            }
                            case DATE: {
                                String dateVal = dateFormat.format(value);
                                cv.put(columnName, dateVal);
                            }
                            break;
                            default:
                                switch (field.getGenericType().toString()) {
                                    case "class java.lang.String":
                                    default:
                                        String str = String.valueOf(value);
                                        cv.put(columnName, str);
                                        break;
                                    case "byte":
                                    case "class java.lang.Byte":
                                        byte byteVal = Byte.parseByte(String.valueOf(value));
                                        cv.put(columnName, byteVal);
                                        break;
                                    case "class [B": {
                                        byte[] bytesVal = (byte[]) value;
                                        cv.put(columnName, bytesVal);
                                    }
                                    break;
                                    case "class [Ljava.lang.Byte;": {
                                        byte[] bytesVal = toPrimitive((Byte[]) value);
                                        cv.put(columnName, bytesVal);
                                    }
                                    break;
                                    case "short":
                                    case "class java.lang.Short":
                                        short shortVal = Short.parseShort(String.valueOf(value));
                                        cv.put(columnName, shortVal);
                                        break;
                                    case "int":
                                    case "class java.lang.Integer":
                                        int intVal = Integer.parseInt(String.valueOf(value));
                                        cv.put(columnName, intVal);
                                        break;
                                    case "long":
                                    case "class java.lang.Long":
                                        long longVal = Long.parseLong(String.valueOf(value));
                                        cv.put(columnName, longVal);
                                        break;
                                    case "boolean":
                                    case "class java.lang.Boolean":
                                        boolean bolVal = Boolean.parseBoolean(String.valueOf(value));
                                        cv.put(columnName, bolVal);
                                        break;
                                    case "float":
                                    case "class java.lang.Float":
                                        float floatVal = Float.parseFloat(String.valueOf(value));
                                        cv.put(columnName, floatVal);
                                        break;
                                    case "double":
                                    case "class java.lang.Double":
                                        double doubleVal = Double.parseDouble(String.valueOf(value));
                                        cv.put(columnName, doubleVal);
                                        break;
                                    case "class java.util.Date":
                                    case "class java.sql.Date":
                                        String dateVal = dateFormat.format(value);
                                        cv.put(columnName, dateVal);
                                        break;
                                }
                        }
                    }
                }
            }
            return cv;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String[] getColumns(List<Field> fields) {
        List<String> columns = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                String columnName = column.columnName();
                if (columnName.equals("")) {
                    columnName = field.getName();
                }
                columns.add(columnName);
            }
        }
        String[] temp = new String[columns.size()];
        columns.toArray(temp);
        return temp;
    }

    /**
     * 获得数据表列index
     *
     * @param fields
     * @param cursor
     * @return
     */
    private Map<String, Integer> getColumnIndex(List<Field> fields, Cursor cursor) {
        Map<String, Integer> columnIndex = new HashMap<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                String columnName = column.columnName();
                if (columnName.equals("")) {
                    columnName = field.getName();
                }
                columnIndex.put(columnName, cursor.getColumnIndex(columnName));
            }
        }
        return columnIndex;
    }

    private T getData(Map<String, Integer> columnIndex, Cursor cursor)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        T d = (T) Class.forName(clazzT.getName()).newInstance();
        Class dClass = d.getClass();
        List<Field> fields = new ArrayList<>();
        Field[] f = dClass.getDeclaredFields();
        fields.addAll(getDataField(f));
//        Class clazz = null;
//        do {
//            if (clazz == null) {
//                clazz = t.getSuperclass();
//            } else {
//                clazz = clazz.getSuperclass();
//            }
//            f = clazz.getDeclaredFields();
//            fields.addAll(Arrays.asList(f));
//        } while (!clazz.getName().equals("java.lang.Object"));
        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                String columnName = column.columnName();
                if (columnName.equals("")) {
                    columnName = field.getName();
                }
                Column.Type columnType = column.columnType();
                field.setAccessible(true);

                switch (columnType) {
                    case STRING: {
                        field.set(d, cursor.getString(columnIndex.get(columnName)));
                    }
                    break;
                    case BYTE: {
                        byte val = (byte) cursor.getInt(columnIndex.get(columnName));
                        field.set(d, val);
                    }
                    break;
                    case BOOLEAN: {
                        boolean val = cursor.getInt(columnIndex.get(columnName)) == 1;
                        field.set(d, val);
                    }
                    break;
                    case INTEGER: {
                        field.set(d, cursor.getInt(columnIndex.get(columnName)));
                    }
                    break;
                    case SHORT: {
                        field.set(d, cursor.getShort(columnIndex.get(columnName)));
                    }
                    break;
                    case LONG: {
                        field.set(d, cursor.getLong(columnIndex.get(columnName)));
                    }
                    break;
                    case BYTES: {
                        field.set(d, cursor.getBlob(columnIndex.get(columnName)));
                    }
                    break;
                    case FLOAT: {
                        field.set(d, cursor.getFloat(columnIndex.get(columnName)));
                    }
                    break;
                    case DOUBLE: {
                        field.set(d, cursor.getDouble(columnIndex.get(columnName)));
                    }
                    break;
                    case DATE: {
                        try {
                            field.set(d, dateFormat.parse(cursor.getString(columnIndex.get(columnName))));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    default:
                        switch (field.getGenericType().toString()) {
                            case "class java.lang.String":
                            default:
                                field.set(d, cursor.getString(columnIndex.get(columnName)));
                                break;
                            case "byte":
                            case "class java.lang.Byte": {
                                byte val = (byte) cursor.getInt(columnIndex.get(columnName));
                                field.set(d, val);
                            }
                            break;
                            case "int":
                            case "class java.lang.Integer":
                                field.set(d, cursor.getInt(columnIndex.get(columnName)));
                                break;
                            case "boolean":
                            case "class java.lang.Boolean": {
                                boolean val = cursor.getInt(columnIndex.get(columnName)) == 1;
                                field.set(d, val);
                            }
                            break;
                            case "class [B":
                                field.set(d, cursor.getBlob(columnIndex.get(columnName)));
                                break;
                            case "class [Ljava.lang.Byte;":
                                byte[] value = cursor.getBlob(columnIndex.get(columnName));
                                Byte[] obj = toObject(value);
                                field.set(d, obj);
                                break;
                            case "short":
                            case "class java.lang.Short":
                                field.set(d, cursor.getShort(columnIndex.get(columnName)));
                                break;
                            case "long":
                            case "class java.lang.Long":
                                field.set(d, cursor.getLong(columnIndex.get(columnName)));
                                break;
                            case "float":
                            case "class java.lang.Float":
                                field.set(d, cursor.getFloat(columnIndex.get(columnName)));
                                break;
                            case "double":
                            case "class java.lang.Double":
                                field.set(d, cursor.getDouble(columnIndex.get(columnName)));
                                break;
                            case "class java.util.Date":
                            case "class java.sql.Date":
                                try {
                                    field.set(d, dateFormat.parse(cursor.getString(columnIndex.get(columnName))));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                }
            }
        }
        return d;
    }

    private List<Field> getDataField(Field[] fields) {
        List<Field> fs = new ArrayList<>(fields.length);
        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                fs.add(field);
            }
        }
        return fs;
    }

    //获取主键
    private String getPrimaryKeyField(List<Field> fields) {
        String primaryKey = null;
        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                String columnName = column.columnName();
                if (columnName.equals("")) {
                    columnName = field.getName();
                }
                boolean isPrimaryKey = column.primaryKey();
                if (isPrimaryKey) {
                    primaryKey = columnName;
                    break;
                }
            }
        }
        return primaryKey;
    }

    //获取主键值
    private String getPrimaryKeyValue(T t, List<Field> fields) {
        String value = null;
        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                boolean isPrimaryKey = column.primaryKey();
                if (isPrimaryKey) {
                    try {
                        field.setAccessible(true);
                        value = (String) field.get(t);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        return value;
    }

    private Byte[] toObject(byte[] array) {
        if (array == null)
            return null;
        if (array.length == 0) {
            return new Byte[0];
        }
        Byte[] result = new Byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Byte.valueOf(array[i]);
        }
        return result;
    }

    private byte[] toPrimitive(Byte[] array) {
        if (array == null)
            return null;
        if (array.length == 0) {
            return new byte[0];
        }
        byte[] result = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].byteValue();
        }
        return result;
    }
}