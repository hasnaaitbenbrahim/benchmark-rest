Run examples:
  jmeter -n -t jmeter/read-heavy.jmx -JbaseUrl=http://localhost:8082 \
    -JinfluxUrl=http://localhost:8086 -JinfluxToken=your_token -JinfluxOrg=perf -JinfluxBucket=jmeter

CSV files:
  jmeter/ids.csv
  jmeter/payloads_1k.csv
  jmeter/payloads_5k.csv
