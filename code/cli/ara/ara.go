package main

import (
	"encoding/json"
	"github.com/urfave/cli/v2"
	"io/ioutil"
	"log"
	"os"
	"path/filepath"
)

const AraWorkspaceDirname = ".ara"
const AraConfigFileName = "config.json"

func main() {
	executionInfo := ExecutionInfo{}
	app := &cli.App{
		Name:    "ara",
		Usage:   "A cli client for ARA",
		Version: "0.0.0",
		Commands: []*cli.Command{
			{
				Name:  "config",
				Usage: "Configure this tool",
				Subcommands: []*cli.Command{
					{
						Name:    "list",
						Aliases: []string{"l"},
						Usage:   "list configuration",
						Action: func(c *cli.Context) error {
							ListConfigs()
							return nil
						},
					},
					{
						Name:    "set",
						Aliases: []string{"s"},
						Usage:   "set a configuration `KEY` `VALUE` with . separator for key",
						Action: func(c *cli.Context) error {
							if c.Args().Present() {
								SetConfig(c.Args().Get(0), c.Args().Get(1))
							} else {
								return cli.Exit("set requires a least one arg (the key)", 2)
							}
							return nil
						},
					},
				},
			},
			{
				Name:  "init",
				Usage: "Initialize ara workspace",
				Action: func(c *cli.Context) error {
					return initAraWorkspace()
				},
			},
			{
				Name:  "reset",
				Usage: "Reset the ara workspace",
				Action: func(c *cli.Context) error {
					removeAraWorkspace()
					return initAraWorkspace()
				},
			},
			{
				Name:  "execution",
				Usage: "Configure an execution",
				Subcommands: []*cli.Command{
					{
						Name:    "create",
						Aliases: []string{"c"},
						Flags: []cli.Flag {
							&cli.StringFlag{
								Name: "type",
								Aliases: []string{"t"},
								Usage: "test type",
								Required: true,
								Destination: &executionInfo.Type,
							},
							&cli.StringFlag{
								Name: "country",
								Aliases: []string{"c"},
								Usage: "country code",
								Required: true,
								Destination: &executionInfo.Country,
							},
							&cli.StringFlag{
								Name: "report",
								Aliases: []string{"r"},
								Usage: "report path",
								Required: true,
								Destination: &executionInfo.ReportPath,
							},
							&cli.StringFlag{
								Name: "step-definition",
								Aliases: []string{"d"},
								Usage: "optional step definition path",
								Destination: &executionInfo.StepDefinitionPath,
							},
							&cli.StringFlag{
								Name: "comment",
								Aliases: []string{"m"},
								Usage: "execution comment",
								Destination: &executionInfo.Comment,
							},
							&cli.StringFlag{
								Name: "job-url",
								Aliases: []string{"j"},
								Usage: "job url",
								Required: true,
								Destination: &executionInfo.JobUrl,
							},
							&cli.IntFlag{
								Name: "job-millis",
								Aliases: []string{"s"},
								Usage: "job millis",
								Required: true,
								Destination: &executionInfo.JobMillis,
							},
						},
						Usage:   "create an execution",
						Action: func(c *cli.Context) error {
							CreateExecution(&executionInfo)
							return nil
						},
					},
				},
			},
		},
	}

	err := app.Run(os.Args)
	if err != nil {
		log.Fatal(err)
	}
}

func removeAraWorkspace() {
	err := os.RemoveAll(AraWorkspaceDirname)
	Check(err)
}

func initAraWorkspace() cli.ExitCoder {
	if _, err := os.Stat(AraWorkspaceDirname); !os.IsNotExist(err) {
		return cli.Exit("This is already an ARA workspace use ara reset to reinitialize the workspace", 2)
	}
	println("ARA workspace was initialized")

	// Create Workspace dir
	err := os.Mkdir(AraWorkspaceDirname, os.ModePerm)
	Check(err)

	jsonEnc, err := json.Marshal(Configuration{})
	Check(err)
	// Create empty config file
	err = ioutil.WriteFile(filepath.Join(AraWorkspaceDirname, AraConfigFileName), jsonEnc, 0644)
	Check(err)
	return nil
}

func Check(e error) {
	if e != nil {
		panic(e)
	}
}
