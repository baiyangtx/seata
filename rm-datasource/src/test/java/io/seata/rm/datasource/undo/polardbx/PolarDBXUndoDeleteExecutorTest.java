/*
 *  Copyright 1999-2019 Seata.io Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.seata.rm.datasource.undo.polardbx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.seata.rm.datasource.sql.struct.Row;
import io.seata.rm.datasource.sql.struct.TableRecords;
import io.seata.rm.datasource.undo.BaseExecutorTest;
import io.seata.rm.datasource.undo.SQLUndoLog;
import io.seata.sqlparser.SQLType;
import io.seata.sqlparser.struct.TableMeta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Undo delete executor test for PolarDB-X
 *
 * @author hsien999
 */
public class PolarDBXUndoDeleteExecutorTest extends BaseExecutorTest {
    private static PolarDBXUndoDeleteExecutor executor;

    @BeforeAll
    public static void init() {
        TableMeta tableMeta = Mockito.mock(TableMeta.class);
        Mockito.when(tableMeta.getPrimaryKeyOnlyName()).thenReturn(Collections.singletonList("id"));
        Mockito.when(tableMeta.getTableName()).thenReturn("table_name");

        TableRecords beforeImage = new TableRecords();
        beforeImage.setTableName("table_name");
        beforeImage.setTableMeta(tableMeta);
        List<Row> beforeRows = new ArrayList<>();
        Row row0 = new Row();
        addField(row0, "id", 1, "12345");
        addField(row0, "age", 1, "1");
        beforeRows.add(row0);
        Row row1 = new Row();
        addField(row1, "id", 1, "12346");
        addField(row1, "age", 1, "1");
        beforeRows.add(row1);
        beforeImage.setRows(beforeRows);

        TableRecords afterImage = new TableRecords();
        afterImage.setTableName("table_name");
        afterImage.setTableMeta(tableMeta);
        List<Row> afterRows = new ArrayList<>();
        afterImage.setRows(afterRows);

        SQLUndoLog sqlUndoLog = new SQLUndoLog();
        sqlUndoLog.setSqlType(SQLType.UPDATE);
        sqlUndoLog.setTableMeta(tableMeta);
        sqlUndoLog.setTableName("table_name");
        sqlUndoLog.setBeforeImage(beforeImage);
        sqlUndoLog.setAfterImage(afterImage);

        executor = new PolarDBXUndoDeleteExecutor(sqlUndoLog);
    }

    @Test
    public void buildUndoSQL() {
        String sql = executor.buildUndoSQL().toUpperCase();
        Assertions.assertNotNull(sql);
        Assertions.assertTrue(sql.contains("INSERT"));
        Assertions.assertTrue(sql.contains("TABLE_NAME"));
        Assertions.assertTrue(sql.contains("ID"));
    }

    @Test
    public void getUndoRows() {
        Assertions.assertEquals(executor.getUndoRows(), executor.getSqlUndoLog().getBeforeImage());
    }
}
