# web
------

xco-web is an easy to use control layer framework, is part of the SOA system, using xml language to describe the controller.

### v1.0.2 

1. 支持本地服务模式

	web.xml:
	<context-param>
		<param-name>tangyuan.resource</param-name>
		<param-value>tangyuan-configuration.xml</param-value>
	</context-param>	
	
	web-config.xml:
	<config-property name="localServiceMode" value="true" />
	
2. 支持URL通过本地服务名映射模式

	web-config.xml:
	<config-property name="urlAutoMappingMode" value="true" />
	
#### 使用

	<c url="abc" transfer="abc"/>
	<c url="abc" />
	<c />