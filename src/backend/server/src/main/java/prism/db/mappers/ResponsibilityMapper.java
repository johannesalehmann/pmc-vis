package prism.db.mappers;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import prism.core.Namespace;
import prism.core.Property.Property;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponsibilityMapper implements RowMapper<Map<String, Double>> {

    private final List<Property> properties;

    public ResponsibilityMapper(List<Property> properties){
        this.properties = properties;
    }

    @Override
    public Map<String, Double> map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        Map<String, Double> responsibilityMap = new HashMap<>();
        ResultSetMetaData rsm = rs.getMetaData();
        for (int i = 1; i <= rsm.getColumnCount();i++){
            String column = rsm.getColumnName(i);
            if (column.startsWith(Namespace.ENTRY_RESP)){
                int l = Integer.parseInt(column.replace(Namespace.ENTRY_RESP, ""));
                responsibilityMap.put(properties.get(l).getName(), rs.getDouble(column));
            }
        }
        return responsibilityMap;
    }
}

