import { DayRecordService, DayRecordTypes } from '@/shared/api/day-record';
import { queryOptions } from '@tanstack/react-query';
import { RecordCategory, RecordFilter, RecordType } from './type';
// import dayjs from 'dayjs';

export class DayRecordQueries {
  static readonly queryKeys = {
    all: ['dayRecords'] as const,
    filtered: (filters: RecordFilter<RecordCategory>) =>
      [...this.queryKeys.all, { filters }] as const,
  };

  private static dayRecordQuery(nickname: string, date: string) {
    return queryOptions({
      queryKey: DayRecordQueries.queryKeys.all,
      queryFn: async (): Promise<DayRecordTypes> => {
        const { data } = await DayRecordService.dayRecordQuery({
          params: {
            nickname,
            date,
          },
        });

        return data.data[0].records;
      },
      enabled: !!nickname,
    });
  }

  private static filteredDayRecordsQuery<T extends RecordCategory>(
    filters: RecordFilter<T>
  ) {
    return queryOptions<RecordType<T>>({
      queryKey: this.queryKeys.filtered(filters),
      queryFn: async (): Promise<RecordType<T>> => {
        const { data } = await DayRecordService.dayRecordQuery({
          params: {
            nickname: filters.nickname,
            date: filters.date,
          },
        });

        // const baseData = data.data.dates[0].records[filters.category];
        // return baseData.map((item) => ({
        //   ...item,
        //   startTime: dayjs(item.startTime).format('HH:mm'),
        //   endTime: item.endTime ? dayjs(item.endTime).format('HH:mm') : null,
        // })) as RecordType<T>;

        return data.data[0].records[filters.category];
      },
      enabled: !!filters.nickname,
    });
  }

  static allRecordsQuery(nickname: string, date: string) {
    return this.dayRecordQuery(nickname, date);
  }

  // 각 카테고리별 편의 메서드 // TODO: 하드코딩 수정
  static mealRecordsQuery(nickname: string, date: string) {
    console.log(nickname, date);
    return this.filteredDayRecordsQuery({
      nickname: '어린이 서원',
      date: '2024-11-11',
      category: 'meal',
    });
  }

  static exerciseRecordsQuery(nickname: string, date: string) {
    console.log(nickname, date);
    return this.filteredDayRecordsQuery({
      nickname: '어린이 서원',
      date: '2024-11-11',
      category: 'exercise',
    });
  }

  static sleepRecordsQuery(nickname: string, date: string) {
    console.log(nickname, date);
    return this.filteredDayRecordsQuery({
      nickname: '어린이 서원',
      date: '2024-11-11',
      category: 'sleep',
    });
  }

  static medicationRecordsQuery(nickname: string, date: string) {
    console.log(nickname, date);
    return this.filteredDayRecordsQuery({
      nickname: '어린이 서원',
      date: '2024-11-11',
      category: 'medication',
    });
  }
}
