/******************************************************************************
 * Copyright (C) 2020 by the ARA Contributors                                 *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * 	 http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 *                                                                            *
 ******************************************************************************/

INSERT INTO `project` (`id`, `code`, `name`, `default_at_startup`)
VALUES (1,'the-demo-project','The Demo Project', 0);

INSERT INTO `country` (`code`, `name`, `id`, `project_id`)
VALUES
	('fr','France',1,1),
	('us','United States',2,1);

INSERT INTO `cycle_definition` (`id`, `name`, `branch`, `branch_position`, `project_id`)
VALUES
	(1,'day','develop',1,1),
	(2,'night','develop',1,1),
	(3,'day','master',2,1);

INSERT INTO `severity` (`code`, `position`, `name`, `short_name`, `default_on_missing`, `initials`, `id`, `project_id`)
VALUES
	('sanity-check',1,'Sanity Check','Sanity Ch.',0,'S.C.',1,1),
	('high',2,'High','High',1,'High',2,1),
	('medium',3,'Medium','Medium',0,'Med.',3,1);
	
INSERT INTO `team` (`id`, `name`, `assignable_to_problems`, `assignable_to_functionalities`, `project_id`)
VALUES
	(1,'Marketing',1,1,1),
	(2,'Catalog',1,1,1),
	(3,'Buy',1,1,1),
	(4,'Accounting',1,1,1),
	(5,'Infrastructure',1,0,1);

INSERT INTO `source` (`code`, `name`, `letter`, `technology`, `vcs_url`, `default_branch`, `postman_country_root_folders`, `id`, `project_id`)
VALUES
	('api','API','A','POSTMAN','https://vcs_url.com/company/ara/tree/{{branch}}/generated-postman-report/src/main/resources/demo/collections/','master',1,1,1),
	('web','Web','W','CUCUMBER','https://vcs_url.com/company/ara/tree/{{branch}}/generated-cucumber-report/src/main/resources/demo/features/','master',0,2,1);

INSERT INTO `type` (`code`, `name`, `is_browser`, `is_mobile`, `source_id`, `id`, `project_id`)
VALUES
	('api','Integ. APIs',0,0,1,1,1),
	('firefox-desktop','HMI Desktop',1,0,2,2,1),
	('firefox-mobile','HMI Mobile',1,1,2,3,1);