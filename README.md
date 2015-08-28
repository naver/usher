Welcome to the usher wiki!

Usher is a graph layout batch tool running on Apache Tomcat. Typically, Layout a graph is an endless job so called as NP complete. Usher uses popular graph layout algorithm Force Atlas of [Gephi-toolkit](https://github.com/gephi/gephi-toolkit). When user provides a list of nodes and a list of edges with JSON format via HTTP request, Usher runs layout algorithm and returns coordinate for each node as JSON. It also caches layout result so if a first request takes several minutes to generate a layout, second request will be done in a few milliseconds just by reading a cached file!

Example:

If Usher is running on http://localhost:28080. You can send a request with a graph data as below(Works on both GET and POST).

```
http://localhost:28080/Usher/layout?data={"nodes":["n1","n2","n3","n4"],"edges":[["n1","n2"],["n1","n3"],["n3","n4"]]}
```

The the response will be like,

```
{"status":"ok","message":"","data":{"n1":[-63.589378,309.603668],"n2":[42.014568,-66.732544],"n3":[170.108322,-20.717154],"n4":[-121.449570,192.134644],"n4":[-10.2,100.3234]}}
```

That's all! Now you can use coordinates wherever you want to use!


