package main

import (
	"fmt"
	"github.com/urfave/cli/v2"
	"io"
	"io/ioutil"
	"os"
	"path/filepath"
	"strconv"
	"syscall"
)

const GenerationDir = "generated"
const Unifier = "/"

type ExecutionInfo struct {
	Type               string
	Country            string
	ReportPath         string
	StepDefinitionPath string
	Comment            string
	JobUrl             string
	JobMillis          int
}

type BuildInformation struct {
	Url              string `json:"url"`
	Result           string `json:"result"`
	Timestamp        string `json:"timestamp"`
	Release          string `json:"release"`
	Version          string `json:"version"`
	VersionTimestamp string `json:"versionTimestamp"`
	Comment          string `json:"comment"`
}

func CreateExecution(executionInfo *ExecutionInfo) cli.ExitCoder {
	config := GetConfig()
	testPath := filepath.Join(GenerationDir, strconv.Itoa(config.Version.Millis), executionInfo.Country, executionInfo.Type)
	os.MkdirAll(testPath, os.ModePerm)

	// COPY report Path
	if info, err := os.Stat(executionInfo.ReportPath); !os.IsNotExist(err) {
		if info.IsDir() {
			CopyDirectory(executionInfo.ReportPath, filepath.Join(testPath, "report"))
		} else {
			Copy(executionInfo.ReportPath, filepath.Join(testPath, filepath.Base(executionInfo.ReportPath)))
		}
	} else {
		return cli.Exit(fmt.Sprintf("Report path %s does not exist", executionInfo.ReportPath), 3)
	}

	// COPY step definition Path
	if info, err := os.Stat(executionInfo.StepDefinitionPath); !os.IsNotExist(err) && !info.IsDir() {
		Copy(executionInfo.StepDefinitionPath, filepath.Join(testPath, filepath.Base(executionInfo.StepDefinitionPath)))
	}

	return nil
}

func CopyDirectory(scrDir, dest string) error {
	entries, err := ioutil.ReadDir(scrDir)
	if err != nil {
		return err
	}
	for _, entry := range entries {
		sourcePath := filepath.Join(scrDir, entry.Name())
		destPath := filepath.Join(dest, entry.Name())

		fileInfo, err := os.Stat(sourcePath)
		if err != nil {
			return err
		}

		stat, ok := fileInfo.Sys().(*syscall.Stat_t)
		if !ok {
			return fmt.Errorf("failed to get raw syscall.Stat_t data for '%s'", sourcePath)
		}

		switch fileInfo.Mode() & os.ModeType{
		case os.ModeDir:
			if err := CreateIfNotExists(destPath, 0755); err != nil {
				return err
			}
			if err := CopyDirectory(sourcePath, destPath); err != nil {
				return err
			}
		case os.ModeSymlink:
			if err := CopySymLink(sourcePath, destPath); err != nil {
				return err
			}
		default:
			if err := Copy(sourcePath, destPath); err != nil {
				return err
			}
		}

		if err := os.Lchown(destPath, int(stat.Uid), int(stat.Gid)); err != nil {
			return err
		}

		isSymlink := entry.Mode()&os.ModeSymlink != 0
		if !isSymlink {
			if err := os.Chmod(destPath, entry.Mode()); err != nil {
				return err
			}
		}
	}
	return nil
}

func Copy(srcFile, dstFile string) error {
	out, err := os.Create(dstFile)
	if err != nil {
		return err
	}

	defer out.Close()

	in, err := os.Open(srcFile)
	defer in.Close()
	if err != nil {
		return err
	}

	_, err = io.Copy(out, in)
	if err != nil {
		return err
	}

	return nil
}

func Exists(filePath string) bool {
	if _, err := os.Stat(filePath); os.IsNotExist(err) {
		return false
	}

	return true
}

func CreateIfNotExists(dir string, perm os.FileMode) error {
	if Exists(dir) {
		return nil
	}

	if err := os.MkdirAll(dir, perm); err != nil {
		return fmt.Errorf("failed to create directory: '%s', error: '%s'", dir, err.Error())
	}

	return nil
}

func CopySymLink(source, dest string) error {
	link, err := os.Readlink(source)
	if err != nil {
		return err
	}
	return os.Symlink(link, dest)
}
