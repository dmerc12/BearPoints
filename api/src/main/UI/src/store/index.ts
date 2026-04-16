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
    fetchStudents, searchStudentsInList, fetchClassroomLeaderboard, fetchStudentById, fetchStudentByToken,
    addStudent, modifyStudent, removeStudent, resetStudents, clearStudentsError, clearSelectedStudent,
    fetchBehaviorTypes, searchBehaviorTypesInList, fetchBehaviorTypeById, addBehaviorType, modifyBehaviorType,
    removeBehaviorType, resetBehaviorTypes, clearBehaviorTypesError, clearSelectedBehaviorType,
    fetchBragLogs, searchBragLogsInList, fetchBragLogById, addBragLog, modifyBragLog, removeBragLog,
    resetBragLogs, clearBragLogsError, clearSelectedBragLog,
    fetchRewardItems, searchRewardItemsInList, fetchRewardItemById, addRewardItem, modifyRewardItem,
    removeRewardItem, resetRewardItems, clearRewardItemsError, clearSelectedRewardItem,
    fetchStudentRewards, searchStudentRewardsInList, fetchStudentRewardById, addStudentReward, modifyStudentReward,
    removeStudentReward, resetStudentRewards, clearStudentRewardsError, clearSelectedStudentReward,
    fetchLeaderboard, setTimeframe, resetLeaderboard, clearTimeframeCache, clearSortCache, clearLeaderboardError
} from './slices';
