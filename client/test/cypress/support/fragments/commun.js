 
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
    }
      
}

