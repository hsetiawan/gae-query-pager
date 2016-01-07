At the present time, the Google App Engine does not provide a built-in pagination support. There are various methods how one can implement limited pagination manually, but writing the paging queries by hand can be tedious and error-prone. Thus [this pager](http://code.google.com/p/gae-query-pager/source/browse/trunk/gae-query-pager/src/com/insose/gae/pager/GaeQueryPager.java) tries to alleviate this problem by providing an automatic way to generate appropriate paging queries from a base query, and then using these queries to page through the data.

The pagination method used is the one described in [this document](http://google-appengine.googlegroups.com/web/efficient_paging_using_key_instead_of_a_dedicated_unique_property.txt).
However the algorithm used was developed from scratch.

This pagination method can not be used for a direct jump to a specific page number. Only continuous forward and backward paging using a bookmark and an unique property is supported.

Due to the fact that the number of composite indexes allowed on the Google App Engine is limited to 100 per app, it is recommended to optimize your queries to have lower composite index requirements. If you use an ordering on a property and the values of this property are not unique, it is always better to have a copy of this property with these values made unique - that way multiple queries will not have to be used, and the composite index requirements will be much lower.

The pager always returns JDO data class instances, however if you use the low-level Java datastore API to efficiently implement optimization techniques using ancestor entities, you can construct a low-level API [GaeQuery](http://code.google.com/p/gae-query-pager/source/browse/trunk/gae-query-pager/src/com/insose/gae/pager/GaeQuery.java), and the pager will use that query for paging on the ancestor table, but it will automatically return JDO instances discovered by calling getParent() on the resulting ancestor entities.

A GaeQuery instance can be constructed automatically by the pager, using a limited subset of the JDOQL query language, or it can be constructed programatically. GaeQuery is a standalone class that can be easily converted to a JDO API query, or to the low-level API query, including the actual parameter values.

To refresh a view to a page, simply reverse the direction and repeat the call.

A brief standalone javadocs [can be found here](http://gae-query-pager.googlecode.com/svn/trunk/gae-query-pager/javadoc/index.html).

Test pager output [can be found here](http://code.google.com/p/gae-query-pager/wiki/TestPagerOutput).