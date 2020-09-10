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


export const getProgressBar = (value, widthMax) => {
    var numPower = Math.pow(10, 6); 
    return ~~(value * widthMax/(100*10000) * numPower)/numPower + 'px';
}

export const testFirstProgressBar = (progressBar, value, widthMax) => {
    return expect(progressBar.find('>div').first()).to.have.css('width', getProgressBar(value, widthMax));
}

export const testLastProgressBar = (progressBar, value, widthMax) => {
    return expect(progressBar.find('>div').last()).to.have.css('width', getProgressBar(value, widthMax));
}

