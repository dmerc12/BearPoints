import studentRewardsReducer from './slices/studentRewardsSlice';
import behaviorTypesReducer from './slices/behaviorTypesSlice';
import rewardItemsReducer from './slices/rewardItemsSlice';
import leaderboardReducer from './slices/leaderboardSlice';
import bragLogsReducer from './slices/bragLogsSlice';
import teachersReducer from './slices/teachersSlice';
import studentsReducer from './slices/studentsSlice';
import { configureStore } from '@reduxjs/toolkit';
import usersReducer from './slices/usersSlice';
import userReducer from './slices/userSlice';

export const store = configureStore({
    reducer: {
        user: userReducer,
        users: usersReducer,
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
    fetchCurrentUser, clearCurrentUser,
    fetchUsers, searchUsersInList, fetchUserById, addUser, modifyUser, removeUser,
    resetUsers, clearUsersError, clearSelectedUser,
    fetchTeachers, searchTeachersInList, fetchTeacherById, addTeacher, modifyTeacher, removeTeacher,
    resetTeachers, clearTeachersError, clearSelectedTeacher,
    fetchStudents, addStudent, modifyStudent, removeStudent, resetStudents, clearStudentsError,
    fetchBehaviorTypes, addBehaviorType, modifyBehaviorType, removeBehaviorType, resetBehaviorTypes, clearBehaviorTypesError,
    fetchBragLogs, addBragLog, addPublicBragLog, modifyBragLog, removeBragLog, resetBragLogs, clearBragLogsError,
    fetchRewardItems, addRewardItem, modifyRewardItem, removeRewardItem, resetRewardItems, clearRewardItemsError,
    fetchStudentRewards, addStudentReward, modifyStudentReward, removeStudentReward, resetStudentRewards, clearStudentRewardsError,
    fetchLeaderboard, setTimeframe, resetLeaderboard, clearTimeframeCache, clearSortCache, clearLeaderboardError
} from './slices';
