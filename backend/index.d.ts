import { DecodedToken } from './src/types';

declare global {
    namespace Express {
        interface Request {
            user?: DecodedToken;
        }
    }
}
