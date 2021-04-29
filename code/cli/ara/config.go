package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"
	"strings"
)

type PlatformRule struct {
	Country      string   `json:"country"`
	CountryTags  []string `json:"countryTags,omitempty"`
	TestTypes    []string `json:"testTypes,omitempty"`
	SeverityTags []string `json:"severityTags,omitempty"`
}

type QualityThreshold struct {
	Failure int `json:"failure"`
	Warning int `json:"warning"`
}

type Version struct {
	PackageName string `json:"packageName,omitempty"`
	Release     string `json:"release,omitempty"`
	Millis      int    `json:"millis,omitempty"`
}

type Configuration struct {
	ServerURL     string                      `json:"serverURL,omitempty"`
	Project       string                      `json:"project,omitempty"`
	Branch        string                      `json:"branch,omitempty"`
	Cycle         string                      `json:"cycle,omitempty"`
	Thresholds    map[string]QualityThreshold `json:"thresholds,omitempty"`
	PlatformRules map[string]PlatformRule     `json:"platformRules,omitempty"`
	Version       Version                     `json:"version,omitempty"`
}

func GetConfig() Configuration {
	file, _ := os.Open(filepath.Join(AraWorkspaceDirname, AraConfigFileName))
	defer file.Close()
	decoder := json.NewDecoder(file)
	configuration := Configuration{}
	err := decoder.Decode(&configuration)
	Check(err)
	return configuration
}

func SetConfig(key string, value string) {
	file, _ := os.Open(filepath.Join(AraWorkspaceDirname, AraConfigFileName))
	defer file.Close()
	decoder := json.NewDecoder(file)
	configuration := make(map[string]interface{})
	err := decoder.Decode(&configuration)
	Check(err)

	setPropertyStructure(strings.Split(key, "."), &value, &configuration)

	jsonEnc, err := json.Marshal(configuration)
	Check(err)
	// Create empty config file
	err = ioutil.WriteFile(filepath.Join(AraWorkspaceDirname, AraConfigFileName), jsonEnc, 0644)
}

func setPropertyStructure(keys []string, value *string, configuration *map[string]interface{}) {
	if len(keys) == 1 {
		var jsonValue interface{}
		err := json.Unmarshal([]byte(*value), &jsonValue)
		Check(err)
		if jsonValue == nil {
			(*configuration)[keys[0]] = *value
		} else {
			(*configuration)[keys[0]] = jsonValue
		}
	} else {
		if (*configuration)[keys[0]] == nil {
			(*configuration)[keys[0]] = make(map[string]interface{})
		}
		subConf := (*configuration)[keys[0]].(map[string]interface{})
		setPropertyStructure(keys[1:], value, &subConf)
	}
}

func ListConfigs() {
	jsonConfig, err := json.MarshalIndent(GetConfig(), "", "  ")
	Check(err)
	fmt.Println(string(jsonConfig))
}
