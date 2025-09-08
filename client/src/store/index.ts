import studentRewardsReducer from './slices/studentRewardsSlice';
import behaviorTypesReducer from './slices/behaviorTypesSlice';
import rewardItemsReducer from './slices/rewardItemsSlice';
import leaderboardReducer from './slices/leaderboardSlice';
import bragLogsReducer from './slices/bragLogsSlice';
import teachersReducer from './slices/teachersSlice';
import studentsReducer from './slices/studentsSlice';
import { configureStore } from '@reduxjs/toolkit';
import adminsReducer from './slices/adminsSlice';
import userReducer from './slices/userSlice';

export const store = configureStore({
    reducer: {
        user: userReducer,
        admins: adminsReducer,
        teachers: teachersReducer,
        students: studentsReducer,
        behaviorTypes: behaviorTypesReducer,
        bragLogs: bragLogsReducer,
        rewardItems: rewardItemsReducer,
        studentRewards: studentRewardsReducer,
        leaderboard: leaderboardReducer,
    },
    middleware: (getDefaultMiddleware) => getDefaultMiddleware({
        serializableCheck: false
    })
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
export { useAppDispatch, useAppSelector } from './hooks';
export {
    fetchAdmins, addAdmin, modifyAdmin, removeAdmin, resetAdmins, clearAdminsError,
    fetchTeachers, addTeacher, modifyTeacher, removeTeacher, resetTeachers,
    fetchBehaviorTypes, addBehaviorType, modifyBehaviorType, removeBehaviorType,
    resetBehaviorTypes, clearBehaviorTypesError,
} from './slices';
