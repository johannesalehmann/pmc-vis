package prism.db.mappers;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import prism.core.Computation.DataProvider;
import prism.core.Namespace;
import prism.core.Property.Property;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * Maps database output for property Maps (i.e. Maps from property name to value in state)
 */
public class PropertyMapper implements RowMapper<Map<String, Map<String, Double>>> {

    private final List<Property> properties;
    private final List<DataProvider> providers;

    public PropertyMapper(List<Property> properties, List<DataProvider> providers){
        this.properties = properties;
        this.providers = providers;
    }

    @Override
    public Map<String, Map<String, Double>> map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        Map<String, Map<String, Double>> propertyMap = new HashMap<>();
        ResultSetMetaData rsm = rs.getMetaData();
        Map<String,Double> map = new HashMap<>();
        for (int i = 1; i <= rsm.getColumnCount();i++){
            String collumn = rsm.getColumnName(i);
            if (collumn.startsWith(Namespace.ENTRY_PROP)){
                int l = Integer.parseInt(collumn.replace(Namespace.ENTRY_PROP, ""));
                map.put(properties.get(l).getName(), rs.getDouble(i));
            }
        }
        propertyMap.put(Namespace.OUTPUT_RESULTS, map);
        for (DataProvider provider : providers){
            map = new HashMap<>();
            Map<String, String> columns = provider.getColumnMap();
            for (int i = 1; i <= rs.getMetaData().getColumnCount();i++){
                String collumn = rsm.getColumnName(i);
                if (columns.containsKey(collumn)){
                    map.put(columns.get(collumn), rs.getDouble(i));
                }
            }
            propertyMap.put(provider.getName(), map);
        }
        return propertyMap;
    }
}
