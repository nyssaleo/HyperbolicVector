COPY region FROM './src/main/resources/data/tpch\region.csv' (FORMAT 'csv', header 1, delimiter ',', quote '"');
COPY nation FROM './src/main/resources/data/tpch\nation.csv' (FORMAT 'csv', header 1, delimiter ',', quote '"');
COPY supplier FROM './src/main/resources/data/tpch\supplier.csv' (FORMAT 'csv', header 1, delimiter ',', quote '"');
COPY customer FROM './src/main/resources/data/tpch\customer.csv' (FORMAT 'csv', header 1, delimiter ',', quote '"');
COPY part FROM './src/main/resources/data/tpch\part.csv' (FORMAT 'csv', header 1, delimiter ',', quote '"');
COPY partsupp FROM './src/main/resources/data/tpch\partsupp.csv' (FORMAT 'csv', header 1, delimiter ',', quote '"');
COPY orders FROM './src/main/resources/data/tpch\orders.csv' (FORMAT 'csv', header 1, delimiter ',', quote '"');
COPY lineitem FROM './src/main/resources/data/tpch\lineitem.csv' (FORMAT 'csv', header 1, delimiter ',', quote '"');
