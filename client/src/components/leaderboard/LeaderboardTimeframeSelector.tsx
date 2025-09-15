import { Button, ButtonGroup } from 'react-bootstrap';
import { Timeframe } from '../../services';

interface LeaderboardTimeframeSelectorProps {
    currentTimeframe: Timeframe;
    onTimeframeChange: (timeframe: Timeframe) => void;
}

export function LeaderboardTimeframeSelector({ currentTimeframe, onTimeframeChange }: LeaderboardTimeframeSelectorProps) {
    return (
        <div className='mb-3 text-center'>
            <ButtonGroup className='mb-3'>
                <Button variant={currentTimeframe ===  Timeframe.WEEK ? 'primary' : 'outline-primary'}
                        onClick={() => onTimeframeChange(Timeframe.WEEK)}
                >
                    Week
                </Button>
                <Button variant={currentTimeframe ===  Timeframe.MONTH ? 'primary' : 'outline-primary'}
                        onClick={() => onTimeframeChange(Timeframe.MONTH)}
                >
                    Month
                </Button>
                <Button variant={currentTimeframe ===  Timeframe.SEMESTER ? 'primary' : 'outline-primary'}
                        onClick={() => onTimeframeChange(Timeframe.SEMESTER)}
                >
                    Semester
                </Button>
                <Button variant={currentTimeframe ===  Timeframe.YEAR ? 'primary' : 'outline-primary'}
                        onClick={() => onTimeframeChange(Timeframe.YEAR)}
                >
                    Year
                </Button>
            </ButtonGroup>
        </div>
    );
}
