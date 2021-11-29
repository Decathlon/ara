DO 
$$
declare
projectID int;
cycleID int;
countryID int;

sourceWebID int;
sourceApiID int;

typeWebID int;
typeApiID int;

execution2JID int;
execution1JID int;
execution0JID int;

run2JApiID int;
run2JWebID int;
run1JApiID int;
run1JWebID int;
run0JApiID int;
run0JWebID int;

problemID int;
problemPatternID int;
begin
	if exists (select 1 from project p where p.code = 'tests-project') then
	return;
	end if;
	-- Add project
	select nextval('project_id') into projectID;
	insert into project(id, code, default_at_startup, "name") values
		(projectID, 'tests-project', true, 'The tests project');
	
	-- Add root cause
	insert into root_cause(id, "name", project_id) values 
		(nextval('root_cause_id'), 'Fragile test', projectID);

	-- Add team
	insert into team(id, assignable_to_functionalities, assignable_to_problems, "name", project_id) values
		(nextval('team_id'), true, true, 'Default Team', projectID);

	-- Add severity
	insert into severity(id, code, default_on_missing, initials, "name", "position", project_id, short_name) values
		(nextval('severity_id'), 'sanity-check', false, 'S.C.', 'Sanity Check', 1, projectID, 'Sanity Ch.'),
		(nextval('severity_id'), 'high', true, 'High', 'High', 2, projectID, 'High'),
		(nextval('severity_id'), 'medium', false, 'Med.', 'Medium', 3, projectID, 'Medium');
	
	-- Add communication
	insert into communication(id, code, message, "name", project_id, "type") values
		(nextval('communication_id'), 'executions', 'This project is generated for testing', 'Executions: Top message', projectID, 'TEXT'),
		(nextval('communication_id'), 'scenario-writing-helps', 'Edit the scenario in Git: update the sql script.', 'Scenario-Writing Helps', projectID, 'TEXT'),
		(nextval('communication_id'), 'howto-add-scenario', 'Features file for cucumber', 'Scenario-Writing Helps: Where to edit or add a scenario?', projectID, 'TEXT');

	-- Add cycle definition
	select nextval('cycle_definition_id') into cycleID;
	insert into cycle_definition(id,branch,branch_position,"name",project_id) values
		(cycleID, 'develop', 1, 'day', projectID);
	
	-- Add country
	select nextval('country_id') into countryID;
	insert into public.country(id, code, "name", project_id) values
		(countryID, 'fr', 'France', projectID);
	
	-- Add sources
	select nextval('source_id') into sourceApiID;
	select nextval('source_id') into sourceWebID;
	insert into "source"(id, code, default_branch, letter, "name", postman_country_root_folders, project_id, technology, vcs_url) values
		(sourceApiID, 'api', 'develop', 'A', 'API', true, projectID, 'POSTMAN', 'https://build.company.com/test/tree/{{branch}}/server/src/main/resources/demo/collections/'),
		(sourceWebID, 'web', 'develop', 'W', 'WEB', false, projectID, 'CUCUMBER', 'https://build.company.com/test/tree/{{branch}}/server/src/main/resources/demo/collections/');
	
	-- Add types
	select nextval('type_id') into typeApiID;
	select nextval('type_id') into typeWebID;
	insert into "type"(id, code, is_browser, is_mobile, "name", project_id, source_id) values
		(typeApiID, 'api', false, false, 'Integ. APIs', projectID, sourceApiID),
		(typeWebID, 'web', true, false, 'WEB', projectID, sourceWebID);
	
	-- Add executions
	select nextval('execution_id') into execution2JID;
	select nextval('execution_id') into execution1JID;
	select nextval('execution_id') into execution0JID;
	insert into execution
		(id				, acceptance, blocking_validation	, branch	, build_date_time			, discard_reason	, duration	, estimated_duration, job_link															, job_url											, "name", quality_severities																																																																																																																																																																																																														 , quality_status	, quality_thresholds																										, "release"	, "result"	, status, test_date_time			, "version"									, cycle_definition_id) values
		(execution2JID	, 'NEW'		, true					, 'develop'	, (NOW() - INTERVAL '2 DAY'), null				, 0			, 0					, '/tmp/ara_test_executions_work_9125052716478554549/1637740800000/', 'https://build.company.com/test/develop/day/5555/', 'day'	, '[{"severity":{"code":"sanity-check","position":1,"name":"Sanity Check","shortName":"Sanity Ch.","initials":"S.C.","defaultOnMissing":false},"scenarioCounts":{"total":9,"failed":7,"passed":2},"percent":22,"status":"FAILED"},{"severity":{"code":"high","position":2,"name":"High","shortName":"High","initials":"High","defaultOnMissing":true},"scenarioCounts":{"total":2,"failed":2,"passed":0},"percent":0,"status":"FAILED"},{"severity":{"code":"medium","position":3,"name":"Medium","shortName":"Medium","initials":"Med.","defaultOnMissing":false},"scenarioCounts":{"total":7,"failed":5,"passed":2},"percent":28,"status":"FAILED"},{"severity":{"code":"*","position":2147483647,"name":"Global","shortName":"Global","initials":"Global","defaultOnMissing":false},"scenarioCounts":{"total":18,"failed":14,"passed":4},"percent":22,"status":"FAILED"}]', 'FAILED'			, '{"sanity-check":{"failure":100,"warning":100},"high":{"failure":95,"warning":98},"medium":{"failure":90,"warning":95}}'	, 'v3'		, 'SUCCESS'	, 'DONE', (NOW() - INTERVAL '2 DAY'), 'cb93dde5560b17e22c84ff98f4ded26c1a967aaf', cycleID),
		(execution1JID	, 'NEW'		, true					, 'develop'	, (NOW() - INTERVAL '1 DAY'), null				, 0			, 0					, '/tmp/ara_test_executions_work_7454335539901426121/1637827200000/', 'https://build.company.com/test/develop/day/5556/', 'day'	, '[{"severity":{"code":"sanity-check","position":1,"name":"Sanity Check","shortName":"Sanity Ch.","initials":"S.C.","defaultOnMissing":false},"scenarioCounts":{"total":9,"failed":8,"passed":1},"percent":11,"status":"FAILED"},{"severity":{"code":"high","position":2,"name":"High","shortName":"High","initials":"High","defaultOnMissing":true},"scenarioCounts":{"total":2,"failed":2,"passed":0},"percent":0,"status":"FAILED"},{"severity":{"code":"medium","position":3,"name":"Medium","shortName":"Medium","initials":"Med.","defaultOnMissing":false},"scenarioCounts":{"total":7,"failed":5,"passed":2},"percent":28,"status":"FAILED"},{"severity":{"code":"*","position":2147483647,"name":"Global","shortName":"Global","initials":"Global","defaultOnMissing":false},"scenarioCounts":{"total":18,"failed":15,"passed":3},"percent":16,"status":"FAILED"}]', 'FAILED'			, '{"sanity-check":{"failure":100,"warning":100},"high":{"failure":95,"warning":98},"medium":{"failure":90,"warning":95}}'	, 'v3'		, 'SUCCESS'	, 'DONE', (NOW() - INTERVAL '1 DAY'), '211ccc3cf6ea2f9e2c9e3bf2905f9b725d66643e', cycleID),
		(execution0JID	, 'NEW'		, true					, 'develop'	, NOW()						, null				, 0			, 0					, '/tmp/ara_test_executions_work_7454335539901426121/1637827200000/', 'https://build.company.com/test/develop/day/5557/', 'day'	, '[{"severity":{"code":"sanity-check","position":1,"name":"Sanity Check","shortName":"Sanity Ch.","initials":"S.C.","defaultOnMissing":false},"scenarioCounts":{"total":9,"failed":8,"passed":1},"percent":11,"status":"FAILED"},{"severity":{"code":"high","position":2,"name":"High","shortName":"High","initials":"High","defaultOnMissing":true},"scenarioCounts":{"total":2,"failed":2,"passed":0},"percent":0,"status":"FAILED"},{"severity":{"code":"medium","position":3,"name":"Medium","shortName":"Medium","initials":"Med.","defaultOnMissing":false},"scenarioCounts":{"total":7,"failed":5,"passed":2},"percent":28,"status":"FAILED"},{"severity":{"code":"*","position":2147483647,"name":"Global","shortName":"Global","initials":"Global","defaultOnMissing":false},"scenarioCounts":{"total":18,"failed":15,"passed":3},"percent":16,"status":"FAILED"}]', 'FAILED'			, '{"sanity-check":{"failure":100,"warning":100},"high":{"failure":95,"warning":98},"medium":{"failure":90,"warning":95}}'	, 'v3'		, 'SUCCESS'	, 'DONE', NOW()						, '4af6d8eb902bebc93d810654d124163b0cf8cbba', cycleID);

	-- Add runs
	select nextval('run_id') into run2JApiID;
	select nextval('run_id') into run2JWebID;
	select nextval('run_id') into run1JApiID;
	select nextval('run_id') into run1JWebID;
	select nextval('run_id') into run0JApiID;
	select nextval('run_id') into run0JWebID;
	insert into run
		(id			, "comment"	, country_tags	, duration	, estimated_duration, execution_id	, include_in_thresholds	, job_link																	, job_url											, platform	, severity_tags	, start_date_time			, status, country_id, type_id) values
		(run2JApiID	, null		, 'all'			, 0			, 0					, execution2JID	, true					, '/tmp/ara_test_executions_work_9125052716478554549/1637740800000/fr/api/'	, 'https://build.company.com/test/develop/5555/'	, 'integ'	, 'all'			, (NOW() - INTERVAL '2 DAY'), 'DONE', countryID	, typeApiID),
		(run2JWebID	, null		, 'all'			, 0			, 0					, execution2JID	, true					, '/tmp/ara_test_executions_work_9125052716478554549/1637740800000/fr/web/'	, 'https://build.company.com/test/develop/5555/'	, 'integ'	, 'all'			, (NOW() - INTERVAL '2 DAY'), 'DONE', countryID	, typeWebID),
		(run1JApiID	, null		, 'all'			, 0			, 0					, execution1JID	, true					, '/tmp/ara_test_executions_work_9125052716478554549/1637740800000/fr/api/'	, 'https://build.company.com/test/develop/5556/'	, 'integ'	, 'all'			, (NOW() - INTERVAL '1 DAY'), 'DONE', countryID	, typeApiID),
		(run1JWebID	, null		, 'all'			, 0			, 0					, execution1JID	, true					, '/tmp/ara_test_executions_work_9125052716478554549/1637740800000/fr/web/'	, 'https://build.company.com/test/develop/5556/'	, 'integ'	, 'all'			, (NOW() - INTERVAL '1 DAY'), 'DONE', countryID	, typeWebID),
		(run0JApiID	, null		, 'all'			, 0			, 0					, execution0JID	, true					, '/tmp/ara_test_executions_work_9125052716478554549/1637740800000/fr/api/'	, 'https://build.company.com/test/develop/5557/'	, 'integ'	, 'all'			, NOW()						, 'DONE', countryID	, typeApiID),
		(run0JWebID	, null		, 'all'			, 0			, 0					, execution0JID	, true					, '/tmp/ara_test_executions_work_9125052716478554549/1637740800000/fr/web/'	, 'https://build.company.com/test/develop/5557/'	, 'integ'	, 'all'			, NOW()						, 'DONE', countryID	, typeWebID);

	-- Add problem
	select nextval('problem_id') into problemID;
	insert into problem
		(id			, closing_date_time	, "comment"	, creation_date_time, defect_existence	, defect_id	, first_seen_date_time	, last_seen_date_time	, "name"				, project_id, status, blamed_team_id, root_cause_id) values
		(problemID	, null				, null		, NOW()				, null				, null		, NOW()					, NOW()					, 'CLASS_NOT_DEF_FOUND'	, projectID	, 'OPEN', null			, null);

	-- Add problem pattern
	select nextval('problem_pattern_id') into problemPatternID;
	insert into problem_pattern(id, "exception", feature_file, feature_name, platform, problem_id, "release", scenario_name, scenario_name_starts_with, step, step_definition, step_definition_starts_with, step_starts_with, type_is_browser, type_is_mobile, country_id, type_id) values
		(problemPatternID, 'java.lang.NoClassDefFoundError: Could not initialize class cucumber.deps.com.thoughtworks.xstream.converters.collections.TreeMapConverter', NULL, NULL, NULL, problemID, NULL, NULL, false, NULL, '^the user has products in cart$', false, false, NULL, NULL, NULL, typeWebID);

	
	-- Add executed scenarios and errors
	insert into executed_scenario
		(id								, api_server, cucumber_id							, cucumber_report_url														, diff_report_url	, feature_file								, feature_name							, feature_tags	, http_requests_url, java_script_errors_url	, line	, logs_url	, "name"												, run_id	, screenshot_url, selenium_node	, severity		, start_date_time			, tags						, video_url	, "content") values
		(nextval('executed_scenario_id'), null		, 'all/List all our useless products'	, 'https://build.company.com/test/develop/5555/Postman_Collection_Results/'	, null				, 'choose-a-product.postman_collection.json', 'Our Lovely Store - Choose a product'	, null			, null				, null					, 12	, null		, 'all ▶ Functionality 3: List all our useless products', run2JApiID, null			, null			, 'sanity-check', (NOW() - INTERVAL '2 DAY'), '@severity-sanity-check'	, null		, '-100000:passed:<Pre-Request Script>
-1:passed:887000000:GET {{baseUrl}}/get
0:passed:Status code is 200
1:passed:The server should return 3 useless products
100000:passed:<Test Script>'),
		(nextval('executed_scenario_id'), null		, 'fr+us/Pay by Card'					, 'https://build.company.com/test/develop/5555/Cucumber_Results/'			, null				, 'pay.feature'								, 'Our Lovely Store - Pay'				, null			, null				, null					, 2		, null		, 'fr+us ▶ Functionalities 15 & 16: Pay by Card'		, run2JWebID, null			, null			, 'sanity-check', (NOW() - INTERVAL '2 DAY'), '@severity-sanity-check'	, null		, '-100000:passed:<Pre-Request Script>
-1:passed:788000000:POST {{baseUrl}}/post
0:passed:Status code is 200
1:passed:Response should validate the payment method is indeed By card
2:passed:Response should indicate a succeed transaction status
100000:passed:<Test Script>');
	insert into error(id, "exception", executed_scenario_id, step, step_definition, step_line) values
		(nextval('error_id'), 'java.lang.NoClassDefFoundError: Could not initialize class cucumber.deps.com.thoughtworks.xstream.converters.collections.TreeMapConverter
	at cucumber.deps.com.thoughtworks.xstream.XStream.setupConverters(XStream.java:820)
	at cucumber.deps.com.thoughtworks.xstream.XStream.<init>(XStream.java:574)
	at cucumber.deps.com.thoughtworks.xstream.XStream.<init>(XStream.java:530)
	at cucumber.runtime.xstream.LocalizedXStreams$LocalizedXStream.<init>(LocalizedXStreams.java:56)
	at cucumber.runtime.xstream.LocalizedXStreams.newXStream(LocalizedXStreams.java:43)
	at cucumber.runtime.xstream.LocalizedXStreams.get(LocalizedXStreams.java:35)
	at cucumber.runtime.StepDefinitionMatch.runStep(StepDefinitionMatch.java:37)
	at cucumber.runtime.Runtime.runStep(Runtime.java:314)
	at cucumber.runtime.model.StepContainer.runStep(StepContainer.java:44)
	at cucumber.runtime.model.StepContainer.runSteps(StepContainer.java:39)
	at cucumber.runtime.model.CucumberScenario.runBackground(CucumberScenario.java:59)
	at cucumber.runtime.model.CucumberScenario.run(CucumberScenario.java:42)
	at cucumber.runtime.model.CucumberFeature.run(CucumberFeature.java:165)
	at cucumber.runtime.Runtime.run(Runtime.java:130)
	at ara.ReportGenerator.lambda$runCucumber$0(ReportGenerator.java:144)
	at ara.ReportGenerator.silentOutputs(ReportGenerator.java:169)
	at ara.ReportGenerator.runCucumber(ReportGenerator.java:138)
	at ara.ReportGenerator.generateReportJson(ReportGenerator.java:87)
	at ara.ReportGenerator.createDemoData(ReportGenerator.java:66)
	at ara.ReportGenerator.main(ReportGenerator.java:47)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:568)
	at org.codehaus.mojo.exec.ExecJavaMojo$1.run(ExecJavaMojo.java:282)
	at java.base/java.lang.Thread.run(Thread.java:833)
	at ✽.Given the user has products in cart(ara/demo/features/pay.feature:4)
', currval('executed_scenario_id'), 'the user has products in cart', '^the user has products in cart$', 4);
	insert into problem_occurrence(problem_pattern_id, error_id) values
		(problemPatternID, currval('error_id'));
	
	insert into executed_scenario
		(id								, api_server, cucumber_id							, cucumber_report_url														, diff_report_url	, feature_file								, feature_name							, feature_tags	, http_requests_url, java_script_errors_url	, line	, logs_url	, "name"												, run_id	, screenshot_url, selenium_node	, severity		, start_date_time			, tags						, video_url	, "content") values
		(nextval('executed_scenario_id'), null		, 'all/List all our useless products'	, 'https://build.company.com/test/develop/5556/Postman_Collection_Results/'	, null				, 'choose-a-product.postman_collection.json', 'Our Lovely Store - Choose a product'	, null			, null				, null					, 12	, null		, 'all ▶ Functionality 3: List all our useless products', run1JApiID, null			, null			, 'sanity-check', (NOW() - INTERVAL '1 DAY'), '@severity-sanity-check'	, null		, '-100000:passed:<Pre-Request Script>
-1:passed:887000000:GET {{baseUrl}}/get
0:passed:Status code is 200
1:passed:The server should return 3 useless products
100000:passed:<Test Script>'),
		(nextval('executed_scenario_id'), null		, 'fr+us/Pay by Card'					, 'https://build.company.com/test/develop/5556/Cucumber_Results/'			, null				, 'pay.feature'								, 'Our Lovely Store - Pay'				, null			, null				, null					, 2		, null		, 'fr+us ▶ Functionalities 15 & 16: Pay by Card'		, run1JWebID, null			, null			, 'sanity-check', (NOW() - INTERVAL '1 DAY'), '@severity-sanity-check'	, null		, '-100000:passed:<Pre-Request Script>
-1:passed:788000000:POST {{baseUrl}}/post
0:passed:Status code is 200
1:passed:Response should validate the payment method is indeed By card
2:passed:Response should indicate a succeed transaction status
100000:passed:<Test Script>');
	insert into error(id, "exception", executed_scenario_id, step, step_definition, step_line) values
		(nextval('error_id'), 'java.lang.NoClassDefFoundError: Could not initialize class cucumber.deps.com.thoughtworks.xstream.converters.collections.TreeMapConverter
	at cucumber.deps.com.thoughtworks.xstream.XStream.setupConverters(XStream.java:820)
	at cucumber.deps.com.thoughtworks.xstream.XStream.<init>(XStream.java:574)
	at cucumber.deps.com.thoughtworks.xstream.XStream.<init>(XStream.java:530)
	at cucumber.runtime.xstream.LocalizedXStreams$LocalizedXStream.<init>(LocalizedXStreams.java:56)
	at cucumber.runtime.xstream.LocalizedXStreams.newXStream(LocalizedXStreams.java:43)
	at cucumber.runtime.xstream.LocalizedXStreams.get(LocalizedXStreams.java:35)
	at cucumber.runtime.StepDefinitionMatch.runStep(StepDefinitionMatch.java:37)
	at cucumber.runtime.Runtime.runStep(Runtime.java:314)
	at cucumber.runtime.model.StepContainer.runStep(StepContainer.java:44)
	at cucumber.runtime.model.StepContainer.runSteps(StepContainer.java:39)
	at cucumber.runtime.model.CucumberScenario.runBackground(CucumberScenario.java:59)
	at cucumber.runtime.model.CucumberScenario.run(CucumberScenario.java:42)
	at cucumber.runtime.model.CucumberFeature.run(CucumberFeature.java:165)
	at cucumber.runtime.Runtime.run(Runtime.java:130)
	at ara.ReportGenerator.lambda$runCucumber$0(ReportGenerator.java:144)
	at ara.ReportGenerator.silentOutputs(ReportGenerator.java:169)
	at ara.ReportGenerator.runCucumber(ReportGenerator.java:138)
	at ara.ReportGenerator.generateReportJson(ReportGenerator.java:87)
	at ara.ReportGenerator.createDemoData(ReportGenerator.java:66)
	at ara.ReportGenerator.main(ReportGenerator.java:47)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:568)
	at org.codehaus.mojo.exec.ExecJavaMojo$1.run(ExecJavaMojo.java:282)
	at java.base/java.lang.Thread.run(Thread.java:833)
	at ✽.Given the user has products in cart(ara/demo/features/pay.feature:4)
', currval('executed_scenario_id'), 'the user has products in cart', '^the user has products in cart$', 4);
	insert into problem_occurrence(problem_pattern_id, error_id) values
		(problemPatternID, currval('error_id'));
	
	insert into executed_scenario
		(id								, api_server, cucumber_id							, cucumber_report_url														, diff_report_url	, feature_file								, feature_name							, feature_tags	, http_requests_url, java_script_errors_url	, line	, logs_url	, "name"												, run_id	, screenshot_url, selenium_node	, severity		, start_date_time			, tags						, video_url	, "content") values
		(nextval('executed_scenario_id'), null		, 'all/List all our useless products'	, 'https://build.company.com/test/develop/5556/Postman_Collection_Results/'	, null				, 'choose-a-product.postman_collection.json', 'Our Lovely Store - Choose a product'	, null			, null				, null					, 12	, null		, 'all ▶ Functionality 3: List all our useless products', run0JApiID, null			, null			, 'sanity-check', NOW()						, '@severity-sanity-check'	, null		, '-100000:passed:<Pre-Request Script>
-1:passed:887000000:GET {{baseUrl}}/get
0:passed:Status code is 200
1:passed:The server should return 3 useless products
100000:passed:<Test Script>'),
		(nextval('executed_scenario_id'), null		, 'fr+us/Pay by Card'					, 'https://build.company.com/test/develop/5556/Cucumber_Results/'			, null				, 'pay.feature'								, 'Our Lovely Store - Pay'				, null			, null				, null					, 2		, null		, 'fr+us ▶ Functionalities 15 & 16: Pay by Card'		, run0JWebID, null			, null			, 'sanity-check', NOW()						, '@severity-sanity-check'	, null		, '-100000:passed:<Pre-Request Script>
-1:passed:788000000:POST {{baseUrl}}/post
0:passed:Status code is 200
1:passed:Response should validate the payment method is indeed By card
2:passed:Response should indicate a succeed transaction status
100000:passed:<Test Script>');
	insert into error(id, "exception", executed_scenario_id, step, step_definition, step_line) values
		(nextval('error_id'), 'java.lang.NoClassDefFoundError: Could not initialize class cucumber.deps.com.thoughtworks.xstream.converters.collections.TreeMapConverter
	at cucumber.deps.com.thoughtworks.xstream.XStream.setupConverters(XStream.java:820)
	at cucumber.deps.com.thoughtworks.xstream.XStream.<init>(XStream.java:574)
	at cucumber.deps.com.thoughtworks.xstream.XStream.<init>(XStream.java:530)
	at cucumber.runtime.xstream.LocalizedXStreams$LocalizedXStream.<init>(LocalizedXStreams.java:56)
	at cucumber.runtime.xstream.LocalizedXStreams.newXStream(LocalizedXStreams.java:43)
	at cucumber.runtime.xstream.LocalizedXStreams.get(LocalizedXStreams.java:35)
	at cucumber.runtime.StepDefinitionMatch.runStep(StepDefinitionMatch.java:37)
	at cucumber.runtime.Runtime.runStep(Runtime.java:314)
	at cucumber.runtime.model.StepContainer.runStep(StepContainer.java:44)
	at cucumber.runtime.model.StepContainer.runSteps(StepContainer.java:39)
	at cucumber.runtime.model.CucumberScenario.runBackground(CucumberScenario.java:59)
	at cucumber.runtime.model.CucumberScenario.run(CucumberScenario.java:42)
	at cucumber.runtime.model.CucumberFeature.run(CucumberFeature.java:165)
	at cucumber.runtime.Runtime.run(Runtime.java:130)
	at ara.ReportGenerator.lambda$runCucumber$0(ReportGenerator.java:144)
	at ara.ReportGenerator.silentOutputs(ReportGenerator.java:169)
	at ara.ReportGenerator.runCucumber(ReportGenerator.java:138)
	at ara.ReportGenerator.generateReportJson(ReportGenerator.java:87)
	at ara.ReportGenerator.createDemoData(ReportGenerator.java:66)
	at ara.ReportGenerator.main(ReportGenerator.java:47)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:568)
	at org.codehaus.mojo.exec.ExecJavaMojo$1.run(ExecJavaMojo.java:282)
	at java.base/java.lang.Thread.run(Thread.java:833)
	at ✽.Given the user has products in cart(ara/demo/features/pay.feature:4)
', currval('executed_scenario_id'), 'the user has products in cart', '^the user has products in cart$', 4);
	insert into problem_occurrence(problem_pattern_id, error_id) values
		(problemPatternID, currval('error_id'));
end;
$$ LANGUAGE PLPGSQL;
