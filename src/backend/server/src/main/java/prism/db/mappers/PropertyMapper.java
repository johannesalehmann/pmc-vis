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
public class PropertyMapper implements RowMapper<Map<String, Map<String, Object>>> {

    private final List<Property> properties;
    private final List<DataProvider> providers;

    public PropertyMapper(List<Property> properties, List<DataProvider> providers){
        this.properties = properties;
        this.providers = providers;
    }

    @Override
    public Map<String, Map<String, Object>> map(final ResultSet rs, final StatementContext ctx) throws SQLException {
        Map<String, Map<String, Object>> propertyMap = new HashMap<>();
        ResultSetMetaData rsm = rs.getMetaData();
        Map<String,Object> map = new HashMap<>();
        for (int i = 1; i <= rsm.getColumnCount();i++){
            String collumn = rsm.getColumnName(i);
            if (collumn.startsWith(Namespace.ENTRY_PROP)){
                int l = Integer.parseInt(collumn.replace(Namespace.ENTRY_PROP, ""));
                map.put(properties.get(l).getName(), rs.getDouble(i));
            }
        }
        propertyMap.put(Namespace.OUTPUT_RESULTS, map);
        for (DataProvider provider : providers){
            Map<String, Map<String, Object>> providerMap = new HashMap<>();
            Map<String, String[]> columns = provider.getColumnMap();
            for (int i = 1; i <= rs.getMetaData().getColumnCount();i++){
                String collumn = rsm.getColumnName(i);
                if (columns.containsKey(collumn)){
                    String category = columns.get(collumn)[0];
                    String prop = columns.get(collumn)[1];
                    if (!providerMap.containsKey(category)){
                        providerMap.put(category, new HashMap<>());
                    }
                    if(provider.isBool()){
                        providerMap.get(category).put(prop, rs.getBoolean(i));
                    }else{
                        providerMap.get(category).put(prop, rs.getDouble(i));
                    }

                }
            }
            propertyMap.putAll(providerMap);
        }
        return propertyMap;
    }
}
