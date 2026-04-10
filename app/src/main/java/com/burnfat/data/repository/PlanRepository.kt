package com.burnfat.data.repository

import com.burnfat.data.local.dao.PlanDao
import com.burnfat.data.local.entity.PlanEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlanRepository @Inject constructor(
    private val planDao: PlanDao
) {
    fun getAllFlow(): Flow<List<PlanEntity>> = planDao.getAllFlow()

    suspend fun getAll(): List<PlanEntity> = planDao.getAll()

    suspend fun getActivePlan(): PlanEntity? = planDao.getActivePlan()

    fun getActivePlanFlow(): Flow<PlanEntity?> = planDao.getActivePlanFlow()

    suspend fun getById(id: Long): PlanEntity? = planDao.getById(id)

    suspend fun insert(plan: PlanEntity): Long = planDao.insert(plan)

    suspend fun update(plan: PlanEntity) = planDao.update(plan)

    suspend fun delete(plan: PlanEntity) = planDao.delete(plan)

    suspend fun activatePlan(id: Long) = planDao.activatePlan(id)

    suspend fun createPlan(
        name: String,
        startWeight: Float,
        targetWeight: Float,
        bmr: Float,
        activityLevel: String,
        targetDate: Long
    ): Long {
        // Deactivate all existing plans
        val timestamp = System.currentTimeMillis()
        val plan = PlanEntity(
            name = name,
            startWeight = startWeight,
            currentWeight = startWeight,
            targetWeight = targetWeight,
            bmr = bmr,
            activityLevel = activityLevel,
            targetDate = targetDate,
            isActive = true,
            createdAt = timestamp,
            updatedAt = timestamp
        )
        return planDao.insert(plan)
    }

    suspend fun getAllPlans(): List<PlanEntity> = planDao.getAll()

    suspend fun updatePlan(
        id: Long,
        name: String,
        startWeight: Float,
        currentWeight: Float,
        targetWeight: Float,
        bmr: Float,
        activityLevel: String,
        targetDate: Long
    ) {
        val plan = planDao.getById(id) ?: return
        planDao.update(plan.copy(
            name = name,
            startWeight = startWeight,
            currentWeight = currentWeight,
            targetWeight = targetWeight,
            bmr = bmr,
            activityLevel = activityLevel,
            targetDate = targetDate,
            updatedAt = System.currentTimeMillis()
        ))
    }

    suspend fun deletePlan(id: Long) {
        val plan = planDao.getById(id) ?: return
        planDao.delete(plan)
    }

    suspend fun createPlan(plan: PlanEntity): Long {
        return planDao.insert(plan)
    }

    suspend fun updateWeight(planId: Long, newWeight: Float, newBmr: Float? = null) {
        val plan = planDao.getById(planId) ?: return
        planDao.update(plan.copy(
            currentWeight = newWeight,
            bmr = newBmr ?: plan.bmr,
            updatedAt = System.currentTimeMillis()
        ))
    }
}