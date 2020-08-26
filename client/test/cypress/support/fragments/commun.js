var GREEN = 'rgb(245, 247, 249)';
var NO_COLOR = 'rgba(0, 0, 0, 0)';
var RED = 'rgb(0, 0, 0)';
export const getRGB = (color) => {
    switch (color) {
        case 'green':
            return GREEN;
            break;
        case 'red':
            return RED;
            break;
        case 'none':
            return NO_COLOR;
            break;
    };
}

var PROGRESS_BAR = 154.281250/100;
export const getProgressBar = (value) => {
    return value * PROGRESS_BAR + 'px';
}

export const testProgressBar = (progressBar, value) => {
    return expect(progressBar.find('>div').first()).to.have.css('width', getProgressBar(value));
}